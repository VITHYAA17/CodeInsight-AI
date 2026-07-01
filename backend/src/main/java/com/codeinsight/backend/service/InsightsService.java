package com.codeinsight.backend.service;

import com.codeinsight.backend.dto.InsightsDTO;
import com.codeinsight.backend.dto.MetricsDTO;
import com.codeinsight.backend.dto.SkillGapDTO;
import com.codeinsight.backend.entity.TopicScores;
import com.codeinsight.backend.repository.TopicScoresRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InsightsService {

    private final TopicScoresRepository topicScoresRepository;
    private final AnalyticsService analyticsService;
    private final InterviewReadinessService interviewReadinessService;

    public InsightsService(TopicScoresRepository topicScoresRepository,
                          AnalyticsService analyticsService,
                          InterviewReadinessService interviewReadinessService) {
        this.topicScoresRepository = topicScoresRepository;
        this.analyticsService = analyticsService;
        this.interviewReadinessService = interviewReadinessService;
    }

    public InsightsDTO generateInsights(Long userId) {
        InsightsDTO insights = new InsightsDTO();

        // Get metrics
        MetricsDTO metrics = analyticsService.calculateMetrics(userId);

        // Calculate interview readiness
        Integer readinessScore = interviewReadinessService.calculateReadiness(userId);
        insights.setInterviewReadiness(readinessScore);

        // Match with companies
        Map<String, Integer> companyMatch = matchCompanies(userId, metrics);
        insights.setCompanyMatch(companyMatch);

        // Generate recommendations
        List<String> recommendations = generateRecommendations(userId, metrics);
        insights.setRecommendations(recommendations);

        // Identify skill gaps
        List<SkillGapDTO> skillGaps = identifySkillGaps(userId);
        insights.setSkillGaps(skillGaps);

        // Set performance level
        String performanceLevel = getPerformanceLevel(readinessScore);
        insights.setPerformanceLevel(performanceLevel);

        // Set next milestone
        String nextMilestone = getNextMilestone(readinessScore);
        insights.setNextMilestone(nextMilestone);

        return insights;
    }

    private Map<String, Integer> matchCompanies(Long userId, MetricsDTO metrics) {
        Map<String, Integer> companyMatch = new LinkedHashMap<>();
        
        Integer baseScore = metrics.getTotalProblems() > 0 ? (metrics.getTotalProblems() / 5) : 0;
        baseScore = Math.min(baseScore, 100);

        // Company skill requirements (simplified)
        companyMatch.put("Google", Math.min(baseScore + 10, 100));
        companyMatch.put("Amazon", Math.min(baseScore + 5, 100));
        companyMatch.put("Microsoft", Math.min(baseScore, 100));
        companyMatch.put("Apple", Math.min(baseScore - 5, 100));
        companyMatch.put("Meta", Math.min(baseScore, 100));
        companyMatch.put("Goldman Sachs", Math.min(baseScore - 10, 100));

        return companyMatch;
    }

    private List<String> generateRecommendations(Long userId, MetricsDTO metrics) {
        List<String> recommendations = new ArrayList<>();

        // Based on problem count
        if (metrics.getTotalProblems() < 100) {
            recommendations.add("🎯 Complete 100+ problems to build strong fundamentals");
        } else if (metrics.getTotalProblems() < 250) {
            recommendations.add("🎯 Aim for 250+ total problems for better coverage");
        }

        // Based on difficulty distribution
        if (metrics.getHardPercentage().compareTo(new BigDecimal(20)) < 0) {
            recommendations.add("💪 Focus on hard-level problems to strengthen advanced concepts");
        }

        // Based on acceptance rate
        if (metrics.getAverageAcceptanceRate().compareTo(new BigDecimal(40)) < 0) {
            recommendations.add("📈 Improve accuracy - focus on understanding concepts before solving");
        }

        // Based on streaks
        if (metrics.getCurrentStreak() < 7) {
            recommendations.add("🔥 Maintain a consistent coding streak (7+ days)");
        }

        // Based on contest participation
        if (metrics.getAverageContestRating() != null && metrics.getAverageContestRating() < 1500) {
            recommendations.add("🏆 Participate in more contests to boost ratings");
        }

        if (recommendations.isEmpty()) {
            recommendations.add("✅ Excellent progress! Keep practicing advanced patterns");
        }

        return recommendations;
    }

    private List<SkillGapDTO> identifySkillGaps(Long userId) {
        List<TopicScores> topics = topicScoresRepository.findByUserId(userId);
        
        return topics.stream()
                .filter(t -> t.getStrengthScore().compareTo(new BigDecimal(70)) < 0)
                .sorted(Comparator.comparing(TopicScores::getStrengthScore))
                .limit(5)
                .map(topic -> {
                    SkillGapDTO gap = new SkillGapDTO();
                    gap.setTopic(topic.getTopicName());
                    gap.setCurrentScore(topic.getStrengthScore());
                    gap.setTargetScore(new BigDecimal(80));
                    
                    // Estimate days based on current score
                    int scoreGap = 80 - topic.getStrengthScore().intValue();
                    gap.setEstimatedDaysToTarget(scoreGap * 2);
                    
                    // Set priority based on score
                    if (topic.getStrengthScore().compareTo(new BigDecimal(40)) < 0) {
                        gap.setPriority("HIGH");
                    } else if (topic.getStrengthScore().compareTo(new BigDecimal(60)) < 0) {
                        gap.setPriority("MEDIUM");
                    } else {
                        gap.setPriority("LOW");
                    }
                    
                    return gap;
                })
                .collect(Collectors.toList());
    }

    private String getPerformanceLevel(Integer readinessScore) {
        if (readinessScore >= 80) {
            return "🌟 Expert";
        } else if (readinessScore >= 60) {
            return "📈 Advanced";
        } else if (readinessScore >= 40) {
            return "📚 Intermediate";
        } else {
            return "🔰 Beginner";
        }
    }

    private String getNextMilestone(Integer readinessScore) {
        if (readinessScore < 30) {
            return "Solve 100 problems";
        } else if (readinessScore < 50) {
            return "Achieve 50+ hard problems";
        } else if (readinessScore < 70) {
            return "Master 5+ DSA topics";
        } else if (readinessScore < 85) {
            return "Participate in 10+ contests";
        } else {
            return "🎉 Ready for FAANG interviews!";
        }
    }
}
