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
     * Build default recommendations fallback based on DSA solved stats and chosen Focus Goal
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

        java.util.List<String> gapTopics = new java.util.ArrayList<>();
        if (insights.getSkillGaps() != null && !insights.getSkillGaps().isEmpty()) {
            insights.getSkillGaps().forEach(gap -> gapTopics.add(gap.getTopic()));
            sb.append("- **Critical Gaps**: Your primary areas for improvement are *")
              .append(String.join(", ", gapTopics)).append("*. Focus on practicing more problems in these categories.\n");
        } else {
            sb.append("- **Critical Gaps**: Master Dynamic Programming, Graph Algorithms, and Trees to cover common problem patterns.\n");
        }
        sb.append("\n");

        sb.append("#### 🛠️ growth Action Plan for *").append(focusGoal).append("*\n\n");

        String lowercaseGoal = focusGoal.toLowerCase();
        if (lowercaseGoal.contains("core dsa") || lowercaseGoal.contains("mastery")) {
            sb.append("1. **Master Foundations**: Begin with **Striver's A2Z DSA Course / Playlist** on YouTube. It is the most structured guide for standard DSA questions.\n");
            sb.append("2. **Pattern Identification**: Solve the **NeetCode 150** list. Focus on identifying common patterns like Sliding Window, Two Pointers, and Breadth-First Search.\n");
            sb.append("3. **Topic Priority**: Since you need to build core mastery, prioritize practicing **Arrays, Strings, and Recursion** before moving to dynamic programming.\n");
            sb.append("4. **Coding Consistency**: Aim to solve at least 1 Easy and 1 Medium problem daily on LeetCode to build muscle memory.");
        } else if (lowercaseGoal.contains("dynamic programming") || lowercaseGoal.contains("graphs")) {
            sb.append("1. **Master Dynamic Programming Intuition**: Watch **Aditya Verma's Dynamic Programming Playlist** on YouTube. His step-by-step breakdown of Knapsack, LCS, and Matrix Chain Multiplication is highly recommended.\n");
            sb.append("2. **Graph Traversals & Algorithms**: Follow **Striver's Graph Series** playlist. Master BFS/DFS, Shortest Paths (Dijkstra, Bellman-Ford), and Topological Sort.\n");
            sb.append("3. **Practice Standard DP**: Start with LeetCode's 'Dynamic Programming' study path. Solve 10-15 standard 1D/2D DP problems before trying complex state optimizations.\n");
            sb.append("4. **Visualization**: Draw recursion trees and trace the memoization table on paper for every DP problem you practice.");
        } else if (lowercaseGoal.contains("speed") || lowercaseGoal.contains("accuracy")) {
            sb.append("1. **Timed Practice**: Practice with virtual contests or set a timer. Try to finish LeetCode Medium problems in under 25 minutes.\n");
            sb.append("2. **SDE Sheet Focus**: Follow **Striver's SDE Sheet** (180 questions). It contains the most frequently asked problems that help build speed in recognizing optimal approaches.\n");
            sb.append("3. **Contest Analytics**: Participate in **LeetCode Weekly & Biweekly Contests**. Review your submissions to see where you lost time (e.g. debugging edge cases or choosing wrong data structures).\n");
            sb.append("4. **Identify Bottlenecks**: If accuracy is low, write modular code helper methods to isolate logic and prevent syntax bugs.");
        } else if (lowercaseGoal.contains("competitive") || lowercaseGoal.contains("contests")) {
            sb.append("1. **Practice Platform**: Shift focus to **Codeforces** and **CSES Problem Set**. CSES is excellent for standard CP algorithms.\n");
            sb.append("2. **Advanced Algorithms**: Learn Segment Trees, Fenwick Trees, Trie representations, and Number Theory algorithms. Watch **Luv's CP Playlist** on YouTube for solid explanations.\n");
            sb.append("3. **Contest Strategy**: Learn to read problem statements quickly, identify constraints, and choose the correct complexity (\n");
            sb.append("   - \\(N \\le 10^5\\) ➔ O(N log N) or O(N)\n");
            sb.append("   - \\(N \\le 10^3\\) ➔ O(N²)\n");
            sb.append("   - \\(N \\le 20\\) ➔ O(2^N) backtracking).\n");
            sb.append("4. **Virtual Contests**: Solve past Div. 2 / Div. 3 contests on Codeforces under timed conditions to practice problem selection.");
        } else if (lowercaseGoal.contains("lld") || lowercaseGoal.contains("oop") || lowercaseGoal.contains("design")) {
            sb.append("1. **Low-Level Design (LLD)**: Watch **Concept & Coding LLD Series** (by Shrayansh) or **Gaurav Sen's System Design** series on YouTube.\n");
            sb.append("2. **SOLID Principles**: Master Single Responsibility, Open-Closed, Liskov Substitution, Interface Segregation, and Dependency Inversion. Implement code demonstrating these in your designs.\n");
            sb.append("3. **Standard LLD Designs**: Practice designing systems like a Parking Lot, Movie Ticket Booking (BookMyShow), Splitwise, and Elevator System.\n");
            sb.append("4. **Design Patterns**: Focus on Factory Pattern, Singleton Pattern, Strategy Pattern, Observer Pattern, and Decorator Pattern.");
        } else {
            sb.append("1. **Standard DSA Practice**: Follow **Striver's A2Z DSA sheet** or **Love Babbar's 450 DSA Sheet**.\n");
            sb.append("2. **Pattern Identification**: Solve standard patterns on NeetCode.\n");
            sb.append("3. **Focus on Gap Areas**: Prioritize your gap topics (*").append(String.join(", ", gapTopics)).append("*).\n");
            sb.append("4. **Daily Streaks**: Maintain daily streaks to remain consistent in problem-solving.");
        }

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
