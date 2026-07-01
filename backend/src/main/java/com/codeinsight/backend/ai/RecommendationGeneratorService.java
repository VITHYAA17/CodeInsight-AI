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
     * Build default recommendations fallback
     */
    private String buildDefaultRecommendations(String targetCompany, InsightsDTO insights) {
        StringBuilder sb = new StringBuilder();
        sb.append("### 💡 Interview Preparation Roadmap for ").append(targetCompany).append("\n\n");
        sb.append("Based on your current readiness score of **").append(insights.getInterviewReadinessScore())
          .append("/100** (Performance Level: *").append(insights.getPerformanceLevel()).append("*), here is your tailored roadmap:\n\n");

        String lowercaseCompany = targetCompany.toLowerCase();
        if (lowercaseCompany.contains("google")) {
            sb.append("1. **Graphs & Advanced DSA (Google Focus)**: Focus heavily on Graph traversals (BFS, DFS), Shortest Paths (Dijkstra), Union-Find, and Topological Sorting. Expect complex DP problems with multi-dimensional states.\n");
            sb.append("2. **Google 'Googlyness' & Behavior**: Prepare stories demonstrating cognitive ability, intellectual humility, bias for action, and navigating ambiguity. Keep technical explanations clear and structured.\n");
            sb.append("3. **System Design at Scale**: Study global scale, load balancing, consensus algorithms (Paxos/Raft), distributed file systems (GFS), and BigTable architectures.\n");
            sb.append("4. **Mock Interview Speed**: Practice solving a brand new medium/hard problem in under 30 minutes, starting with a brute face explanation followed by optimal complexity optimization.");
        } else if (lowercaseCompany.contains("amazon")) {
            sb.append("1. **Amazon Leadership Principles (LPs)**: Master the 16 Leadership Principles (especially *Customer Obsession*, *Ownership*, and *Dive Deep*). Format all behavioral answers strictly using the STAR format.\n");
            sb.append("2. **Object-Oriented Design (OOD/LLD)**: Amazon heavily tests Low-Level Design. Practice modeling systems like a parking lot, movie ticket booking, or an online shopping portal using SOLID design principles.\n");
            sb.append("3. **Master Tree and Heap Problems**: Focus on Trie representation, Binary Trees (LCA, diameter), and Heap-based optimization (K-way merge, running median).\n");
            sb.append("4. **System Design (HLD)**: Focus on caching layers, database replication, CDN distribution, load balancing, and SQL vs NoSQL schema designs for scale.");
        } else if (lowercaseCompany.contains("meta") || lowercaseCompany.contains("facebook")) {
            sb.append("1. **High-Speed Coding (Meta Standard)**: Meta technical interviews are famous for speed. You are expected to solve 2 medium-level problems in 40 minutes. Prioritize sliding window, two pointers, and stacks.\n");
            sb.append("2. **Facebook Scale System Design**: Study newsfeed architecture, live chat server design, global search indexers, cache coherence, and Memcached scaling.\n");
            sb.append("3. **Graph Traversals & Trees**: Practice tree serialization/deserialization, LCA of BST, recursive back-tracking, and queue-based level order traversals.\n");
            sb.append("4. **Meta Culture & Performance**: Show eagerness to take impact, move fast, build social connection, and handle critical feedback constructively.");
        } else if (lowercaseCompany.contains("microsoft")) {
            sb.append("1. **Pointer manipulation & Linked Lists**: Microsoft interviews frequently ask pointer manipulations, reversing lists, cycle detection, sorting lists, and multi-dimensional array operations.\n");
            sb.append("2. **Azure Services & System Design**: Study service decomposition (microservices), API gateway design, distributed caching, partition strategies, and high availability systems.\n");
            sb.append("3. **Binary Search & Sorting**: Practice custom sorting keys, binary search variations (upper bound, lower bound, rotated arrays), and greedy algorithms.\n");
            sb.append("4. **Behavioral Preparation**: Frame stories emphasizing collaboration, growth mindset, user empathy, and driving results through technical excellence.");
        } else {
            sb.append("1. **Master Core DSA**: Focus on dynamic programming, graph search (BFS/DFS), and tree traversals. These topics constitute over 60% of technical questions at ").append(targetCompany).append(".\n");
            sb.append("2. **Mock Interviews & Speed**: Try to solve medium-level problems on LeetCode under 25 minutes. Focus on writing clean code and explaining your approach out loud.\n");
            sb.append("3. **System Design & LLD**: Study scalable architectures, database partitioning, caching strategies, and common object-oriented design patterns.\n");
            sb.append("4. **Behavioral Focus**: Frame your previous projects using the STAR method (Situation, Task, Action, Result) highlighting leadership and technical problem-solving.");
        }
        return sb.toString();
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
