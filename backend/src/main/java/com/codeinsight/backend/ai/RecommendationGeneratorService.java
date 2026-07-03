package com.codeinsight.backend.ai;

import com.codeinsight.backend.dto.InsightsDTO;
import com.codeinsight.backend.dto.MetricsDTO;
import com.codeinsight.backend.entity.Recommendation;
import com.codeinsight.backend.entity.User;
import com.codeinsight.backend.repository.RecommendationRepository;
import com.codeinsight.backend.repository.UserRepository;
import com.codeinsight.backend.service.AnalyticsService;
import com.codeinsight.backend.service.InsightsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class RecommendationGeneratorService {

    private final LlmService llmService;
    private final RecommendationRepository recommendationRepository;
    private final UserRepository userRepository;
    private final InsightsService insightsService;
    private final AnalyticsService analyticsService;

    public RecommendationGeneratorService(LlmService llmService,
                                        RecommendationRepository recommendationRepository,
                                        UserRepository userRepository,
                                        InsightsService insightsService,
                                        AnalyticsService analyticsService) {
        this.llmService = llmService;
        this.recommendationRepository = recommendationRepository;
        this.userRepository = userRepository;
        this.insightsService = insightsService;
        this.analyticsService = analyticsService;
    }

    /**
     * Generate AI-powered interview recommendations for a target company
     * @param userId User ID
     * @param targetCompany Target company name (e.g., "Amazon", "Google", "Microsoft")
     * @return Generated Recommendation entity
     */
    public Recommendation generateRecommendations(Long userId, String targetCompany) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Get current insights
        InsightsDTO insights = insightsService.generateInsights(userId);
        MetricsDTO metrics = analyticsService.calculateMetrics(userId);

        // Build user profile string
        String userProfile = buildUserProfile(user, insights, metrics, targetCompany);

        // Get recommendation prompt
        String prompt = PromptTemplates.getRecommendationPrompt(userProfile, targetCompany);

        // Generate content using LLM
        String recommendationText = null;
        try {
            recommendationText = llmService.generateContent(prompt);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(RecommendationGeneratorService.class)
                .warn("OpenAI API call failed, using default structured fallback recommendations: {}", e.getMessage());
        }

        if (recommendationText == null) {
            recommendationText = buildDefaultRecommendations(targetCompany, insights);
        }

        // Create and save recommendation entity
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
     * Build default recommendations fallback based on DSA solved stats
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
        } else {
            sb.append("- **Top Strengths**: Master fundamental topics to identify core strengths.\n");
        }

        if (insights.getSkillGaps() != null && !insights.getSkillGaps().isEmpty()) {
            java.util.List<String> gapTopics = insights.getSkillGaps().stream()
                    .map(com.codeinsight.backend.dto.SkillGapDTO::getTopic)
                    .toList();
            sb.append("- **Critical Gaps**: Your primary areas for improvement are *")
              .append(String.join(", ", gapTopics)).append("*. Focus on practicing more problems in these categories.\n");
        } else {
            sb.append("- **Critical Gaps**: Master Dynamic Programming, Graph Algorithms, and Trees to cover common problem patterns.\n");
        }
        sb.append("\n");

        sb.append("#### 🛠️ Growth Action Plan\n");
        sb.append("1. **Targeted Topic Practice**: Allocate 70% of your coding time to master your critical gap topics. Start with classic standard problems before moving to complex variations.\n");
        sb.append("2. **Difficulty Progression**: Aim to solve at least 2 Medium problems for every 1 Easy problem to push your problem-solving limits.\n");
        sb.append("3. **Daily Consistency**: Maintain a regular streak. Solving 1-2 problems daily is significantly more effective than cramming once a week.\n");
        sb.append("4. **Explain your Logic**: Practice writing pseudocode and describing runtime complexities (Time and Space) for every solution.");

        return sb.toString();
    }

    /**
     * Build comprehensive user profile string for AI context
     */
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
        if (insights.getSkillGaps() != null && !insights.getSkillGaps().isEmpty()) {
            insights.getSkillGaps().forEach(gap ->
                profile.append("- ").append(gap.getTopic())
                       .append(" (Current Score: ").append(gap.getCurrentScore())
                       .append("%, Target: ").append(gap.getTargetScore()).append("%)\n")
            );
        }
        profile.append("\nUser's Target Focus Goal: ").append(focusGoal).append("\n");

        return profile.toString();
    }
}
