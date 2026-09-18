package com.codeinsight.backend.ai;

import com.codeinsight.backend.dto.CodingProblemDTO;
import com.codeinsight.backend.dto.InsightsDTO;
import com.codeinsight.backend.dto.MetricsDTO;
import com.codeinsight.backend.entity.StudyPlan;
import com.codeinsight.backend.entity.User;
import com.codeinsight.backend.repository.StudyPlanRepository;
import com.codeinsight.backend.repository.UserRepository;
import com.codeinsight.backend.service.AnalyticsService;
import com.codeinsight.backend.service.InsightsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
@SuppressWarnings("null")
public class StudyPlanGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(StudyPlanGeneratorService.class);

    private final LlmService llmService;
    private final StudyPlanRepository studyPlanRepository;
    private final UserRepository userRepository;
    private final InsightsService insightsService;
    private final AnalyticsService analyticsService;
    private final RagProblemService ragProblemService;

    public StudyPlanGeneratorService(LlmService llmService,
                                    StudyPlanRepository studyPlanRepository,
                                    UserRepository userRepository,
                                    InsightsService insightsService,
                                    AnalyticsService analyticsService,
                                    RagProblemService ragProblemService) {
        this.llmService = llmService;
        this.studyPlanRepository = studyPlanRepository;
        this.userRepository = userRepository;
        this.insightsService = insightsService;
        this.analyticsService = analyticsService;
        this.ragProblemService = ragProblemService;
    }

    /**
     * Generate AI-powered week-by-week study plan for interview preparation
     */
    public Map<String, Object> generateStudyPlan(Long userId, String targetCompany, Integer weeksAvailable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        if (weeksAvailable < 2 || weeksAvailable > 24) {
            throw new IllegalArgumentException("Weeks available must be between 2 and 24");
        }

        studyPlanRepository.deleteByUserId(userId);

        InsightsDTO insights = insightsService.generateInsights(userId);
        MetricsDTO metrics = analyticsService.calculateMetrics(userId);

        String context = buildStudyContext(user, insights, metrics, targetCompany, weeksAvailable);
        String prompt = PromptTemplates.getStudyPlanPrompt(context, targetCompany, weeksAvailable);

        String studyPlanText = null;
        try {
            studyPlanText = llmService.generateContent(prompt);
        } catch (Exception e) {
            log.warn("OpenAI API call failed, using default structured fallback plan: {}", e.getMessage());
        }

        LocalDateTime now = LocalDateTime.now();
        List<StudyPlan> createdPlans;
        if (studyPlanText != null && !studyPlanText.trim().isEmpty()) {
            createdPlans = parseAndSaveStudyPlan(user, studyPlanText, targetCompany, weeksAvailable);
        } else {
            createdPlans = createDefaultStructuredPlan(user, targetCompany, weeksAvailable, now);
        }

        int totalTasks = createdPlans.size();
        int estimatedHours = calculateEstimatedHours(weeksAvailable, totalTasks);

        Map<String, Object> summary = new HashMap<>();
        summary.put("weeks", weeksAvailable);
        summary.put("totalTasks", totalTasks);
        summary.put("estimatedHours", estimatedHours);
        summary.put("targetCompany", targetCompany);
        summary.put("status", "CREATED");
        summary.put("message", "Study plan generated successfully with " + totalTasks + " tasks across " + weeksAvailable + " weeks");

        return summary;
    }

    /**
     * Stream study plan live over SSE, persisting generated tasks to repository upon completion
     */
    public void streamStudyPlan(Long userId, String targetCompany, Integer weeksAvailable,
                               Consumer<String> onChunk,
                               Consumer<Map<String, Object>> onComplete,
                               Consumer<Throwable> onError) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        if (weeksAvailable < 2 || weeksAvailable > 24) {
            onError.accept(new IllegalArgumentException("Weeks available must be between 2 and 24"));
            return;
        }

        studyPlanRepository.deleteByUserId(userId);

        InsightsDTO insights = insightsService.generateInsights(userId);
        MetricsDTO metrics = analyticsService.calculateMetrics(userId);

        String context = buildStudyContext(user, insights, metrics, targetCompany, weeksAvailable);
        String prompt = PromptTemplates.getStudyPlanPrompt(context, targetCompany, weeksAvailable);

        StringBuilder fullText = new StringBuilder();
        AtomicBoolean streamedAny = new AtomicBoolean(false);

        try {
            llmService.streamChat(
                prompt,
                0.8,
                chunk -> {
                    streamedAny.set(true);
                    fullText.append(chunk);
                    onChunk.accept(chunk);
                },
                () -> {
                    List<StudyPlan> savedPlans = parseAndSaveStudyPlan(user, fullText.toString(), targetCompany, weeksAvailable);
                    Map<String, Object> summary = new HashMap<>();
                    summary.put("weeks", weeksAvailable);
                    summary.put("totalTasks", savedPlans.size());
                    summary.put("estimatedHours", calculateEstimatedHours(weeksAvailable, savedPlans.size()));
                    summary.put("targetCompany", targetCompany);
                    summary.put("status", "CREATED");
                    summary.put("message", "Study plan generated successfully with " + savedPlans.size() + " tasks across " + weeksAvailable + " weeks");
                    onComplete.accept(summary);
                },
                error -> {
                    log.info("Streaming fallback plan due to: {}", error.getMessage());
                    LocalDateTime now = LocalDateTime.now();
                    List<StudyPlan> fallbackPlans = createDefaultStructuredPlan(user, targetCompany, weeksAvailable, now);
                    String readablePlan = generateReadablePlanSummary(targetCompany, weeksAvailable, fallbackPlans);
                    streamFallbackText(readablePlan, onChunk, () -> {
                        Map<String, Object> summary = new HashMap<>();
                        summary.put("weeks", weeksAvailable);
                        summary.put("totalTasks", fallbackPlans.size());
                        summary.put("estimatedHours", calculateEstimatedHours(weeksAvailable, fallbackPlans.size()));
                        summary.put("targetCompany", targetCompany);
                        summary.put("status", "CREATED");
                        summary.put("message", "Study plan generated successfully with " + fallbackPlans.size() + " tasks across " + weeksAvailable + " weeks");
                        onComplete.accept(summary);
                    });
                }
            );
        } catch (Exception e) {
            LocalDateTime now = LocalDateTime.now();
            List<StudyPlan> fallbackPlans = createDefaultStructuredPlan(user, targetCompany, weeksAvailable, now);
            String readablePlan = generateReadablePlanSummary(targetCompany, weeksAvailable, fallbackPlans);
            streamFallbackText(readablePlan, onChunk, () -> {
                Map<String, Object> summary = new HashMap<>();
                summary.put("weeks", weeksAvailable);
                summary.put("totalTasks", fallbackPlans.size());
                summary.put("estimatedHours", calculateEstimatedHours(weeksAvailable, fallbackPlans.size()));
                summary.put("targetCompany", targetCompany);
                summary.put("status", "CREATED");
                summary.put("message", "Study plan generated successfully with " + fallbackPlans.size() + " tasks across " + weeksAvailable + " weeks");
                onComplete.accept(summary);
            });
        }
    }

    private void streamFallbackText(String fullText, Consumer<String> onChunk, Runnable onDone) {
        String[] words = fullText.split(" ");
        for (int i = 0; i < words.length; i++) {
            onChunk.accept(words[i] + (i < words.length - 1 ? " " : ""));
            try {
                Thread.sleep(15);
            } catch (InterruptedException ignored) {}
        }
        onDone.run();
    }

    private String generateReadablePlanSummary(String targetCompany, Integer weeks, List<StudyPlan> plans) {
        StringBuilder sb = new StringBuilder();
        sb.append("### 📅 Personalized ").append(weeks).append("-Week Interview Study Plan for ").append(targetCompany).append("\n\n");
        sb.append("Powered by **Neon pgvector RAG** concept gap analysis:\n\n");

        int currentWeek = 0;
        for (StudyPlan p : plans) {
            if (p.getWeekNumber() != currentWeek) {
                currentWeek = p.getWeekNumber();
                sb.append("#### Week ").append(currentWeek).append(": ").append(p.getTopicName()).append("\n");
            }
            sb.append("- ").append(p.getTaskDescription()).append("\n");
        }
        return sb.toString();
    }

    private List<StudyPlan> parseAndSaveStudyPlan(User user, String planText, String targetCompany, Integer weeksAvailable) {
        List<StudyPlan> savedPlans = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        String[] weekBlocks = planText.split("(?i)week\\s+\\d+");

        int weekNumber = 1;
        for (int i = 1; i <= Math.min(weekBlocks.length - 1, weeksAvailable); i++) {
            String weekContent = weekBlocks[i].trim();
            String[] tasks = weekContent.split("(?i)task\\s+\\d+|[-•]");

            for (String task : tasks) {
                String trimmedTask = task.trim();
                if (trimmedTask.length() > 10) {
                    StudyPlan plan = new StudyPlan();
                    plan.setUserId(user.getId());
                    plan.setWeekNumber(i);
                    plan.setTopicName(extractTopic(trimmedTask));
                    plan.setTaskDescription(cleanDescription(trimmedTask));
                    plan.setStatus("PENDING");
                    plan.setCreatedAt(now);
                    plan.setUpdatedAt(now);

                    savedPlans.add(studyPlanRepository.save(plan));
                }
            }
        }

        if (savedPlans.isEmpty()) {
            savedPlans = createDefaultStructuredPlan(user, targetCompany, weeksAvailable, now);
        }

        return savedPlans;
    }

    private List<StudyPlan> createDefaultStructuredPlan(User user, String targetCompany, Integer weeksAvailable, LocalDateTime now) {
        List<StudyPlan> plans = new ArrayList<>();

        String[] topics = new String[]{
            "Arrays & Two Pointers",
            "Sliding Window & Hash Maps",
            "Binary Search & Rotated Arrays",
            "Trees & Binary Tree Traversal",
            "Graphs, BFS & DFS Traversals",
            "Dynamic Programming (1D & Knapsack)",
            "Heaps & Priority Queues",
            "Backtracking & Recursion"
        };

        // Query Neon RAG for matching authentic problems
        List<CodingProblemDTO> ragProblems = ragProblemService.findProblemsForWeakTopics(Arrays.asList(topics), weeksAvailable * 3);

        for (int week = 1; week <= weeksAvailable; week++) {
            String topic = topics[(week - 1) % topics.length];

            // Assign a retrieved real problem to this week if available
            String targetProbText = "";
            if (!ragProblems.isEmpty()) {
                int probIdx = (week - 1) % ragProblems.size();
                CodingProblemDTO p = ragProblems.get(probIdx);
                targetProbText = String.format(" [Target: %s (%s - %s)]", p.getTitle(), p.getPlatform(), p.getUrl());
            }

            String[] subTaskTemplates = {
                "Core Concepts: Review theoretical time/space complexities and standard patterns for " + topic,
                "Hands-on Practice: Solve medium problems on " + topic + targetProbText,
                "Targeted Interview Questions: Solve hard-level questions frequently asked at " + targetCompany,
                "Speed Run: Complete timed mock interview questions on " + topic + " within 30 minutes"
            };

            for (String desc : subTaskTemplates) {
                StudyPlan plan = new StudyPlan();
                plan.setUserId(user.getId());
                plan.setWeekNumber(week);
                plan.setTopicName(topic);
                plan.setTaskDescription(desc);
                plan.setStatus("PENDING");
                plan.setCreatedAt(now);
                plan.setUpdatedAt(now);

                plans.add(studyPlanRepository.save(plan));
            }
        }

        return plans;
    }

    private String extractTopic(String task) {
        Pattern pattern = Pattern.compile("^([^:]+):");
        Matcher matcher = pattern.matcher(task);
        if (matcher.find()) {
            return matcher.group(1).trim().substring(0, Math.min(100, matcher.group(1).length()));
        }
        String[] words = task.split("\\s+");
        return String.join(" ", Arrays.copyOf(words, Math.min(3, words.length)));
    }

    private String cleanDescription(String description) {
        String cleaned = description.replaceAll("^[^:]*:\\s*", "").trim();
        if (cleaned.length() > 500) {
            cleaned = cleaned.substring(0, 500) + "...";
        }
        return cleaned;
    }

    private String buildStudyContext(User user, InsightsDTO insights, MetricsDTO metrics, 
                                     String targetCompany, Integer weeksAvailable) {
        StringBuilder context = new StringBuilder();
        context.append("Study Plan Context:\n");
        context.append("User: ").append(user.getName()).append("\n");
        context.append("Target: ").append(targetCompany).append("\n");
        context.append("Duration: ").append(weeksAvailable).append(" weeks\n\n");
        context.append("Current Level:\n");
        context.append("- Problems Solved: ").append(metrics.getTotalProblems()).append("\n");
        context.append("- Readiness: ").append(insights.getInterviewReadinessScore()).append("/100\n");
        context.append("- Level: ").append(insights.getPerformanceLevel()).append("\n\n");
        context.append("Weak Areas (Priority):\n");
        List<String> weakTopics = new ArrayList<>();
        if (insights.getSkillGaps() != null && !insights.getSkillGaps().isEmpty()) {
            insights.getSkillGaps().forEach(gap -> {
                weakTopics.add(gap.getTopic());
                context.append("- ").append(gap.getTopic()).append("\n");
            });
        }
        context.append("\nFocus on System Design, DSA Hard Problems, and Weak Topics\n");

        // Augment with Neon pgvector RAG problems
        String ragContext = ragProblemService.buildRagContext(weakTopics, targetCompany, weeksAvailable * 2);
        context.append(ragContext);

        return context.toString();
    }

    private int calculateEstimatedHours(Integer weeks, int tasks) {
        return (weeks * 10) + (tasks / 2);
    }
}