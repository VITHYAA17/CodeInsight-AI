package com.codeinsight.backend.ai;

import com.codeinsight.backend.dto.InsightsDTO;
import com.codeinsight.backend.dto.MetricsDTO;
import com.codeinsight.backend.entity.StudyPlan;
import com.codeinsight.backend.entity.User;
import com.codeinsight.backend.repository.StudyPlanRepository;
import com.codeinsight.backend.repository.UserRepository;
import com.codeinsight.backend.service.AnalyticsService;
import com.codeinsight.backend.service.InsightsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class StudyPlanGeneratorService {

    private final LlmService llmService;
    private final StudyPlanRepository studyPlanRepository;
    private final UserRepository userRepository;
    private final InsightsService insightsService;
    private final AnalyticsService analyticsService;

    public StudyPlanGeneratorService(LlmService llmService,
                                    StudyPlanRepository studyPlanRepository,
                                    UserRepository userRepository,
                                    InsightsService insightsService,
                                    AnalyticsService analyticsService) {
        this.llmService = llmService;
        this.studyPlanRepository = studyPlanRepository;
        this.userRepository = userRepository;
        this.insightsService = insightsService;
        this.analyticsService = analyticsService;
    }

    /**
     * Generate AI-powered week-by-week study plan for interview preparation
     * @param userId User ID
     * @param targetCompany Target company name
     * @param weeksAvailable Number of weeks available for preparation
     * @return Map with plan summary (weeks, totalTasks, estimatedHours)
     */
    public Map<String, Object> generateStudyPlan(Long userId, String targetCompany, Integer weeksAvailable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Validate weeks
        if (weeksAvailable < 2 || weeksAvailable > 24) {
            throw new IllegalArgumentException("Weeks available must be between 2 and 24");
        }

        // Clear existing study plans for this user first to avoid duplication
        studyPlanRepository.deleteByUserId(userId);

        // Get current insights
        InsightsDTO insights = insightsService.generateInsights(userId);
        MetricsDTO metrics = analyticsService.calculateMetrics(userId);

        // Build study plan context
        String context = buildStudyContext(user, insights, metrics, targetCompany, weeksAvailable);

        // Get study plan prompt
        String prompt = PromptTemplates.getStudyPlanPrompt(context, targetCompany, weeksAvailable);

        // Generate study plan using LLM
        String studyPlanText = null;
        try {
            studyPlanText = llmService.generateContent(prompt);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(StudyPlanGeneratorService.class)
                .warn("OpenAI API call failed, using default structured fallback plan: {}", e.getMessage());
        }

        // Parse and save study plan to database
        LocalDateTime now = LocalDateTime.now();
        List<StudyPlan> createdPlans;
        if (studyPlanText != null) {
            createdPlans = parseAndSaveStudyPlan(user, studyPlanText, targetCompany, weeksAvailable);
        } else {
            createdPlans = createDefaultStructuredPlan(user, targetCompany, weeksAvailable, now);
        }

        // Calculate plan summary
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
     * Parse AI-generated study plan and save to database
     */
    private List<StudyPlan> parseAndSaveStudyPlan(User user, String planText, String targetCompany, Integer weeksAvailable) {
        List<StudyPlan> savedPlans = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // Parse week-by-week tasks from AI response
        // Expected format: Week 1: ..., Week 2: ..., etc.
        String[] weekBlocks = planText.split("(?i)week\\s+\\d+");

        int weekNumber = 1;
        for (int i = 1; i <= Math.min(weekBlocks.length - 1, weeksAvailable); i++) {
            String weekContent = weekBlocks[i].trim();

            // Parse multiple tasks from week content
            String[] tasks = weekContent.split("(?i)task\\s+\\d+|[-•]");
            for (String task : tasks) {
                task = task.trim();
                if (task.isEmpty() || task.length() < 10) continue;

                // Extract topic (usually before the colon)
                String topic = extractTopic(task);
                String description = cleanDescription(task);

                StudyPlan plan = new StudyPlan();
                plan.setUserId(user.getId());
                plan.setWeekNumber(weekNumber);
                plan.setTopicName(topic);
                plan.setTaskDescription(description);
                plan.setStatus("PENDING");
                plan.setCreatedAt(now);
                plan.setUpdatedAt(now);

                StudyPlan saved = studyPlanRepository.save(plan);
                savedPlans.add(saved);
            }

            weekNumber++;
        }

        // If parsing didn't generate enough tasks, create default structured tasks
        if (savedPlans.isEmpty()) {
            savedPlans.addAll(createDefaultStructuredPlan(user, targetCompany, weeksAvailable, now));
        }

        return savedPlans;
    }

    /**
     * Create default structured study plan if AI parsing fails
     */
    private List<StudyPlan> createDefaultStructuredPlan(User user, String targetCompany, Integer weeksAvailable, LocalDateTime now) {
        List<StudyPlan> plans = new ArrayList<>();
        String[] topics;
        String lowercaseCompany = targetCompany.toLowerCase();
        if (lowercaseCompany.contains("google")) {
            topics = new String[]{
                "Graphs (BFS/DFS & Topological Sort)",
                "Dynamic Programming & Memoization",
                "Trees, Binary Search Trees & Heaps",
                "Advanced Recursion & Backtracking",
                "Arrays, Two Pointers & Sliding Window",
                "Google System Design (Scale & Global Latency)",
                "String Processing & Trie Data Structures",
                "Google-specific Mock Coding & Go/C++ Best Practices"
            };
        } else if (lowercaseCompany.contains("amazon")) {
            topics = new String[]{
                "System Design & Object-Oriented Design (LLD)",
                "Hash Tables, Maps & Sets",
                "Trees & Tries (Prefix Trees)",
                "Arrays & Binary Search (Divide & Conquer)",
                "Greedy Algorithms & Priority Queues",
                "Dynamic Programming (Knapsack & Grid Problems)",
                "Graphs (Shortest Paths & MST)",
                "Amazon Leadership Principles & Behavioral Star Method"
            };
        } else if (lowercaseCompany.contains("meta") || lowercaseCompany.contains("facebook")) {
            topics = new String[]{
                "Two Pointers & Sliding Window",
                "Stacks & Queues (Monotonic Stack)",
                "Binary Trees & Graph Traversals",
                "System Design (Caching, CDN & Feed Architecture)",
                "Recursion & Backtracking (Permutations)",
                "Arrays & Hash Maps Optimization",
                "Sorting, Binary Search & K-Way Merge",
                "Meta Architecture Mock Interviews"
            };
        } else if (lowercaseCompany.contains("microsoft")) {
            topics = new String[]{
                "Linked Lists & Core Pointer Manipulation",
                "System Design (Azure Cloud Services & Microservices)",
                "Binary Search Trees & Heap Operations",
                "Sorting, Searching & String Matching",
                "Bit Manipulation & Math Problems",
                "Dynamic Programming & Recursion",
                "Arrays & Multi-dimensional Matrix Problems",
                "Microsoft Culture & Behavioral Preparation"
            };
        } else {
            topics = new String[]{
                "Arrays & Strings",
                "Linked Lists & Pointer Manipulation",
                "Stacks & Queues",
                "Trees & Graph Traversals",
                "Dynamic Programming",
                "Sorting & Searching",
                "Hash Tables & Collision Resolution",
                "Heaps & Priority Queues",
                "Greedy Algorithms",
                "Backtracking & Exhaustive Search",
                "System Design Fundamentals",
                "Bit Manipulation"
            };
        }

        for (int week = 1; week <= weeksAvailable; week++) {
            String topic = topics[(week - 1) % topics.length];
            
            String[] subTaskTemplates = {
                "Core Concepts: Review theory, complexities, and standard patterns for %s",
                "Medium Practice: Solve 5-7 medium-level %s problems on LeetCode/Codeforces",
                "Hard Practice: Complete 2-3 hard-level %s challenges asked in %s interviews",
                "Mock Interview: Explain and write clean solutions for %s under 30 minutes"
            };

            for (String template : subTaskTemplates) {
                String desc;
                if (template.contains("%s interviews")) {
                    desc = String.format(template, topic, targetCompany);
                } else {
                    desc = String.format(template, topic);
                }

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

    /**
     * Extract topic from task description
     */
    private String extractTopic(String task) {
        // Try to extract text before colon
        Pattern pattern = Pattern.compile("^([^:]+):");
        Matcher matcher = pattern.matcher(task);
        if (matcher.find()) {
            return matcher.group(1).trim().substring(0, Math.min(100, matcher.group(1).length()));
        }
        // Return first few words
        String[] words = task.split("\\s+");
        return String.join(" ", Arrays.copyOf(words, Math.min(3, words.length)));
    }

    /**
     * Clean task description
     */
    private String cleanDescription(String description) {
        String cleaned = description.replaceAll("^[^:]*:\\s*", "").trim();
        if (cleaned.length() > 500) {
            cleaned = cleaned.substring(0, 500) + "...";
        }
        return cleaned;
    }

    /**
     * Build comprehensive study context for AI prompt
     */
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
        if (insights.getSkillGaps() != null && !insights.getSkillGaps().isEmpty()) {
            insights.getSkillGaps().forEach(gap -> context.append("- ").append(gap).append("\n"));
        }
        context.append("\nFocus on System Design, DSA Hard Problems, and Weak Topics");

        return context.toString();
    }

    /**
     * Calculate estimated hours for study plan
     */
    private int calculateEstimatedHours(Integer weeks, int tasks) {
        // Rough estimate: 1 week = 10 hours, 1 task = 1-2 hours
        return (weeks * 10) + (tasks / 2);
    }
}
