package com.codeinsight.backend.ai;

import com.codeinsight.backend.dto.CodingProblemDTO;
import com.codeinsight.backend.dto.InsightsDTO;
import com.codeinsight.backend.dto.MetricsDTO;
import com.codeinsight.backend.entity.Recommendation;
import com.codeinsight.backend.entity.User;
import com.codeinsight.backend.repository.RecommendationRepository;
import com.codeinsight.backend.repository.UserRepository;
import com.codeinsight.backend.service.AnalyticsService;
import com.codeinsight.backend.service.InsightsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Service
@Transactional
@SuppressWarnings("null")
public class RecommendationGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationGeneratorService.class);

    private final LlmService llmService;
    private final RecommendationRepository recommendationRepository;
    private final UserRepository userRepository;
    private final InsightsService insightsService;
    private final AnalyticsService analyticsService;
    private final RagProblemService ragProblemService;

    public RecommendationGeneratorService(LlmService llmService,
                                        RecommendationRepository recommendationRepository,
                                        UserRepository userRepository,
                                        InsightsService insightsService,
                                        AnalyticsService analyticsService,
                                        RagProblemService ragProblemService) {
        this.llmService = llmService;
        this.recommendationRepository = recommendationRepository;
        this.userRepository = userRepository;
        this.insightsService = insightsService;
        this.analyticsService = analyticsService;
        this.ragProblemService = ragProblemService;
    }

    /**
     * Generate AI-powered interview recommendations for a target company (synchronous)
     */
    public Recommendation generateRecommendations(Long userId, String targetCompany) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        InsightsDTO insights = insightsService.generateInsights(userId);
        MetricsDTO metrics = analyticsService.calculateMetrics(userId);

        String userProfile = buildUserProfile(user, insights, metrics, targetCompany);
        String prompt = PromptTemplates.getRecommendationPrompt(userProfile, targetCompany);

        String recommendationText = null;
        try {
            recommendationText = llmService.generateContent(prompt);
        } catch (Exception e) {
            log.warn("OpenAI API call failed, using RAG structured recommendations: {}", e.getMessage());
        }

        if (recommendationText == null || recommendationText.trim().isEmpty()) {
            recommendationText = buildDefaultRecommendations(targetCompany, insights);
        }

        Recommendation recommendation = new Recommendation();
        recommendation.setUser(user);
        recommendation.setTargetCompany(targetCompany);
        recommendation.setRecommendationText(recommendationText);

        int companyMatchScore = insights.getCompanyMatchingScores().getOrDefault(
            targetCompany, insights.getInterviewReadinessScore()
        );
        recommendation.setInterviewReadiness(new java.math.BigDecimal(companyMatchScore));
        recommendation.setGeneratedAt(LocalDateTime.now());
        recommendation.setCreatedAt(LocalDateTime.now());
        recommendation.setUpdatedAt(LocalDateTime.now());

        return recommendationRepository.save(recommendation);
    }

    /**
     * Stream recommendations in real-time with SSE, saving to database when complete
     */
    public void streamRecommendations(Long userId, String targetCompany,
                                      Consumer<String> onChunk,
                                      Consumer<Recommendation> onComplete,
                                      Consumer<Throwable> onError) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        InsightsDTO insights = insightsService.generateInsights(userId);
        MetricsDTO metrics = analyticsService.calculateMetrics(userId);

        String userProfile = buildUserProfile(user, insights, metrics, targetCompany);
        String prompt = PromptTemplates.getRecommendationPrompt(userProfile, targetCompany);

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
                    Recommendation saved = persistRecommendation(user, targetCompany, fullText.toString(), insights);
                    onComplete.accept(saved);
                },
                error -> {
                    log.info("Live streaming fallback to RAG structured content: {}", error.getMessage());
                    // If stream didn't produce chunks, stream fallback word-by-word with typewriter pacing
                    String fallback = buildDefaultRecommendations(targetCompany, insights);
                    streamFallbackText(fallback, onChunk, () -> {
                        Recommendation saved = persistRecommendation(user, targetCompany, fallback, insights);
                        onComplete.accept(saved);
                    });
                }
            );
        } catch (Exception e) {
            String fallback = buildDefaultRecommendations(targetCompany, insights);
            streamFallbackText(fallback, onChunk, () -> {
                Recommendation saved = persistRecommendation(user, targetCompany, fallback, insights);
                onComplete.accept(saved);
            });
        }
    }

    private Recommendation persistRecommendation(User user, String targetCompany, String text, InsightsDTO insights) {
        Recommendation recommendation = new Recommendation();
        recommendation.setUser(user);
        recommendation.setTargetCompany(targetCompany);
        recommendation.setRecommendationText(text);

        int companyMatchScore = insights.getCompanyMatchingScores().getOrDefault(
            targetCompany, insights.getInterviewReadinessScore()
        );
        recommendation.setInterviewReadiness(new java.math.BigDecimal(companyMatchScore));
        recommendation.setGeneratedAt(LocalDateTime.now());
        recommendation.setCreatedAt(LocalDateTime.now());
        recommendation.setUpdatedAt(LocalDateTime.now());

        return recommendationRepository.save(recommendation);
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

    /**
     * Build default recommendations fallback enriched with Neon pgvector RAG problems
     */
    private String buildDefaultRecommendations(String focusGoal, InsightsDTO insights) {
        StringBuilder sb = new StringBuilder();
        sb.append("### 💡 DSA & Problem Solving Growth Roadmap (Goal: ").append(focusGoal).append(")\n\n");
        sb.append("Based on your current skill analysis (Overall Score: **").append(insights.getInterviewReadinessScore())
          .append("/100**, Performance Level: *").append(insights.getPerformanceLevel()).append("*), here is your customized roadmap for DSA skill improvement:\n\n");

        sb.append("#### 📊 Current Performance Insights\n");
        if (insights.getTopicStrengths() != null && !insights.getTopicStrengths().isEmpty()) {
            sb.append("- **Top Strengths**: You are demonstrating strong capability in *")
              .append(String.join(", ", insights.getTopicStrengths())).append("*.\n");
        }

        List<String> gapTopics = new ArrayList<>();
        if (insights.getSkillGaps() != null && !insights.getSkillGaps().isEmpty()) {
            for (var gap : insights.getSkillGaps()) {
                gapTopics.add(gap.getTopic());
            }
            sb.append("- **Identified Skill Gaps**: Your primary areas requiring practice are *")
              .append(String.join(", ", gapTopics)).append("*.\n");
        }
        sb.append("\n");

        // Neon pgvector RAG Problem Retrieval
        List<CodingProblemDTO> retrievedProblems = ragProblemService.findProblemsForWeakTopics(gapTopics, 4);
        if (!retrievedProblems.isEmpty()) {
            sb.append("#### 🎯 Curated Practice Problems (Neon pgvector RAG)\n");
            sb.append("Based on vector semantic search of your weak concepts, practice these authentic problems:\n");
            for (int i = 0; i < retrievedProblems.size(); i++) {
                CodingProblemDTO p = retrievedProblems.get(i);
                sb.append(String.format("%d. **[%s](%s)** (%s | %s) — %s [Semantic Match: %.0f%%]\n",
                    i + 1, p.getTitle(), p.getUrl(), p.getPlatform(), p.getDifficulty(), p.getDescription(), p.getSimilarity() * 100));
            }
            sb.append("\n");
        }

        sb.append("#### 🚀 Actionable Strategy for ").append(focusGoal).append("\n");
        String lowercaseGoal = focusGoal.toLowerCase();
        if (lowercaseGoal.contains("faang") || lowercaseGoal.contains("interview") || lowercaseGoal.contains("speed")) {
            sb.append("1. **Timed Practice**: Practice with virtual contests or set a timer. Finish LeetCode Medium problems in under 25 minutes.\n");
            sb.append("2. **SDE Sheet Focus**: Follow **Striver's SDE Sheet** (180 questions) to recognize optimal patterns quickly.\n");
            sb.append("3. **Contest Analytics**: Participate in **LeetCode Weekly Contests** and review submission bottlenecks.\n");
            sb.append("4. **Identify Bottlenecks**: Write modular helper methods to isolate logic and eliminate syntax bugs.");
        } else if (lowercaseGoal.contains("competitive") || lowercaseGoal.contains("contests")) {
            sb.append("1. **Practice Platform**: Shift focus to **Codeforces** and **CSES Problem Set**.\n");
            sb.append("2. **Advanced Algorithms**: Master Segment Trees, Fenwick Trees, Trie representations, and Number Theory.\n");
            sb.append("3. **Contest Strategy**: Identify constraints quickly and determine time complexity upfront.\n");
            sb.append("4. **Virtual Contests**: Solve past Div. 2 / Div. 3 contests under timed conditions.");
        } else {
            sb.append("1. **Standard DSA Practice**: Follow **Striver's A2Z DSA sheet** or **Love Babbar's 450 DSA Sheet**.\n");
            sb.append("2. **Pattern Identification**: Focus on recognizing standard patterns on NeetCode.\n");
            sb.append("3. **Focus on Gap Areas**: Prioritize your gap topics (*").append(String.join(", ", gapTopics)).append("*).\n");
            sb.append("4. **Daily Streaks**: Maintain daily consistency in problem-solving.");
        }

        return sb.toString();
    }

    private String buildUserProfile(User user, InsightsDTO insights, MetricsDTO metrics, String focusGoal) {
        StringBuilder profile = new StringBuilder();
        profile.append("User Profile:\n");
        profile.append("Name: ").append(user.getName()).append("\n");
        profile.append("Email: ").append(user.getEmail()).append("\n");
        profile.append("\nPerformance Metrics:\n");
        profile.append("- Total Problems Solved: ").append(metrics.getTotalProblems()).append("\n");
        profile.append("- Easy: ").append(metrics.getEasyPercentage()).append("%\n");
        profile.append("- Medium: ").append(metrics.getMediumPercentage()).append("%\n");
        profile.append("- Hard: ").append(metrics.getHardPercentage()).append("%\n");
        profile.append("- Average Acceptance Rate: ").append(metrics.getAverageAcceptanceRate()).append("%\n");
        profile.append("- Current Streak: ").append(metrics.getMaxCurrentStreak()).append(" days\n");
        profile.append("- Average Contest Rating: ").append(metrics.getAverageContestRating()).append("\n");
        profile.append("\nInterview Readiness:\n");
        profile.append("- Overall Score: ").append(insights.getInterviewReadinessScore()).append("/100\n");
        profile.append("- Performance Level: ").append(insights.getPerformanceLevel()).append("\n");
        profile.append("\nTop Strengths:\n");
        if (insights.getTopicStrengths() != null && !insights.getTopicStrengths().isEmpty()) {
            insights.getTopicStrengths().forEach(strength ->
                profile.append("- ").append(strength).append("\n")
            );
        }
        profile.append("\nAreas for Improvement:\n");
        List<String> weakTopics = new ArrayList<>();
        if (insights.getSkillGaps() != null && !insights.getSkillGaps().isEmpty()) {
            insights.getSkillGaps().forEach(gap -> {
                weakTopics.add(gap.getTopic());
                profile.append("- ").append(gap.getTopic())
                       .append(" (Current Score: ").append(gap.getCurrentScore())
                       .append("%, Target: ").append(gap.getTargetScore()).append("%)\n");
            });
        }
        profile.append("\nUser's Target Focus Goal: ").append(focusGoal).append("\n");

        // Augment with Neon pgvector RAG problems
        String ragContext = ragProblemService.buildRagContext(weakTopics, focusGoal, 4);
        profile.append(ragContext);

        return profile.toString();
    }
}