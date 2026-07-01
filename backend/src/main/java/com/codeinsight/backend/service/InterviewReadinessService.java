package com.codeinsight.backend.service;

import com.codeinsight.backend.dto.MetricsDTO;
import com.codeinsight.backend.entity.TopicScores;
import com.codeinsight.backend.repository.ContestHistoryRepository;
import com.codeinsight.backend.repository.TopicScoresRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class InterviewReadinessService {

    private final AnalyticsService analyticsService;
    private final TopicScoresRepository topicScoresRepository;
    private final ContestHistoryRepository contestHistoryRepository;

    public InterviewReadinessService(AnalyticsService analyticsService,
                                    TopicScoresRepository topicScoresRepository,
                                    ContestHistoryRepository contestHistoryRepository) {
        this.analyticsService = analyticsService;
        this.topicScoresRepository = topicScoresRepository;
        this.contestHistoryRepository = contestHistoryRepository;
    }

    public Integer calculateReadiness(Long userId) {
        MetricsDTO metrics = analyticsService.calculateMetrics(userId);
        
        int readinessScore = 0;

        // Factor 1: Total problems solved (max 25 points)
        int totalProblems = metrics.getTotalProblems() != null ? metrics.getTotalProblems() : 0;
        if (totalProblems >= 500) {
            readinessScore += 25;
        } else if (totalProblems >= 300) {
            readinessScore += 20;
        } else if (totalProblems >= 150) {
            readinessScore += 15;
        } else if (totalProblems >= 50) {
            readinessScore += 10;
        } else if (totalProblems > 0) {
            readinessScore += 5;
        }

        // Factor 2: Hard problem percentage (max 20 points)
        BigDecimal hardPercentage = metrics.getHardPercentage() != null ? metrics.getHardPercentage() : BigDecimal.ZERO;
        if (hardPercentage.compareTo(new BigDecimal(30)) >= 0) {
            readinessScore += 20;
        } else if (hardPercentage.compareTo(new BigDecimal(20)) >= 0) {
            readinessScore += 15;
        } else if (hardPercentage.compareTo(new BigDecimal(10)) >= 0) {
            readinessScore += 10;
        } else if (hardPercentage.compareTo(BigDecimal.ZERO) > 0) {
            readinessScore += 5;
        }

        // Factor 3: Acceptance rate (max 15 points)
        BigDecimal acceptanceRate = metrics.getAverageAcceptanceRate() != null ? metrics.getAverageAcceptanceRate() : BigDecimal.ZERO;
        if (acceptanceRate.compareTo(new BigDecimal(60)) >= 0) {
            readinessScore += 15;
        } else if (acceptanceRate.compareTo(new BigDecimal(50)) >= 0) {
            readinessScore += 12;
        } else if (acceptanceRate.compareTo(new BigDecimal(40)) >= 0) {
            readinessScore += 8;
        } else if (acceptanceRate.compareTo(new BigDecimal(30)) >= 0) {
            readinessScore += 4;
        }

        // Factor 4: Topic diversity (max 15 points)
        List<TopicScores> topicScores = topicScoresRepository.findByUserId(userId);
        int strongTopics = (int) topicScores.stream()
                .filter(t -> t.getStrengthScore().compareTo(new BigDecimal(70)) >= 0)
                .count();

        if (strongTopics >= 8) {
            readinessScore += 15;
        } else if (strongTopics >= 6) {
            readinessScore += 12;
        } else if (strongTopics >= 4) {
            readinessScore += 8;
        } else if (strongTopics >= 2) {
            readinessScore += 4;
        }

        // Factor 5: Contest participation (max 15 points)
        long contestCount = contestHistoryRepository.findByUserId(userId).size();
        if (contestCount >= 20) {
            readinessScore += 15;
        } else if (contestCount >= 10) {
            readinessScore += 12;
        } else if (contestCount >= 5) {
            readinessScore += 8;
        } else if (contestCount > 0) {
            readinessScore += 4;
        }

        // Factor 6: Consistency (max 10 points)
        Integer streak = metrics.getMaxCurrentStreak() != null ? metrics.getMaxCurrentStreak() : 0;
        if (streak >= 30) {
            readinessScore += 10;
        } else if (streak >= 14) {
            readinessScore += 8;
        } else if (streak >= 7) {
            readinessScore += 5;
        } else if (streak >= 3) {
            readinessScore += 2;
        }

        // Ensure score is between 0-100
        return Math.min(Math.max(readinessScore, 0), 100);
    }
}
