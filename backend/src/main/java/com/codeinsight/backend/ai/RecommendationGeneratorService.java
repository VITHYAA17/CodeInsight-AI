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
        String recommendationText = llmService.generateContent(prompt);

        // Create and save recommendation entity
        Recommendation recommendation = new Recommendation();
        recommendation.setUser(user);
        recommendation.setTargetCompany(targetCompany);
        recommendation.setRecommendationText(recommendationText);
        recommendation.setInterviewReadiness(
            new java.math.BigDecimal(insights.getInterviewReadinessScore())
        );
        recommendation.setGeneratedAt(LocalDateTime.now());
        recommendation.setCreatedAt(LocalDateTime.now());
        recommendation.setUpdatedAt(LocalDateTime.now());

        return recommendationRepository.save(recommendation);
    }

    /**
     * Build comprehensive user profile string for AI context
     */
    private String buildUserProfile(User user, InsightsDTO insights, MetricsDTO metrics, String targetCompany) {
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
                profile.append("- ").append(gap).append("\n")
            );
        }
        profile.append("\nTarget Company: ").append(targetCompany).append("\n");
        profile.append("Company Readiness Score: ").append(insights.getCompanyMatchingScores().getOrDefault(targetCompany, 0)).append("/100\n");

        return profile.toString();
    }
}
