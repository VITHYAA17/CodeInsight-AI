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

    /**
     * Calculate company-specific readiness score dynamically based on rigorous metrics
     */
    public Integer calculateCompanyReadiness(Long userId, String company) {
        MetricsDTO metrics = analyticsService.calculateMetrics(userId);
        List<TopicScores> topics = topicScoresRepository.findByUserId(userId);
        long uniqueCount = topics.stream().map(TopicScores::getTopicName).distinct().count();
        if (topics.isEmpty() || uniqueCount < topics.size()) {
            initializeTopicScores(userId);
            topics = topicScoresRepository.findByUserId(userId);
        }

        java.util.Map<String, Integer> strengths = new java.util.HashMap<>();
        for (TopicScores ts : topics) {
            strengths.put(ts.getTopicName(), ts.getStrengthScore().intValue());
        }

        // Get standard components
        int totalProblems = metrics.getTotalProblems() != null ? metrics.getTotalProblems() : 0;
        BigDecimal hardPercentage = metrics.getHardPercentage() != null ? metrics.getHardPercentage() : BigDecimal.ZERO;
        BigDecimal mediumPercentage = metrics.getMediumPercentage() != null ? metrics.getMediumPercentage() : BigDecimal.ZERO;
        BigDecimal acceptanceRate = metrics.getAverageAcceptanceRate() != null ? metrics.getAverageAcceptanceRate() : BigDecimal.ZERO;
        Integer streak = metrics.getMaxCurrentStreak() != null ? metrics.getMaxCurrentStreak() : 0;
        long contestCount = contestHistoryRepository.findByUserId(userId).size();

        // Standardize metrics into a 0-100 score for calculations
        // 1. Total Problems score (0-100)
        double totalProblemsScore = Math.min((totalProblems / 5.0) * 100, 100.0); // e.g. 500 solved = 100%

        // 2. Hard Percentage score (0-100)
        double hardPercScore = Math.min((hardPercentage.doubleValue() / 30.0) * 100, 100.0); // 30% hard = 100%

        // 3. Medium Percentage score (0-100)
        double mediumPercScore = Math.min((mediumPercentage.doubleValue() / 50.0) * 100, 100.0); // 50% medium = 100%

        // 4. Acceptance Rate score (0-100)
        double acceptanceScore = acceptanceRate.doubleValue();

        // 5. Contest participation score (0-100)
        double contestScore = Math.min((contestCount / 20.0) * 100, 100.0); // 20 contests = 100%

        // 6. Streak consistency score (0-100)
        double streakScore = Math.min((streak / 30.0) * 100, 100.0); // 30 day streak = 100%

        double matchScore = 0.0;
        String lowercaseCompany = company.toLowerCase();

        if (lowercaseCompany.contains("google")) {
            // Google loves Graphs, DP, Hard problems, and contest speed
            double targetTopicAvg = (strengths.getOrDefault("Trees & Graphs", 50) + strengths.getOrDefault("Dynamic Programming", 50)) / 2.0;
            matchScore = (targetTopicAvg * 0.35) + (hardPercScore * 0.25) + (totalProblemsScore * 0.15) + (contestScore * 0.15) + (acceptanceScore * 0.10);
        } else if (lowercaseCompany.contains("amazon")) {
            // Amazon loves LLD (Stacks/Queues/Hash Tables), Medium problems, and consistency
            double targetTopicAvg = (strengths.getOrDefault("Hash Tables", 50) + strengths.getOrDefault("Stacks & Queues", 50)) / 2.0;
            matchScore = (targetTopicAvg * 0.35) + (mediumPercScore * 0.25) + (totalProblemsScore * 0.15) + (streakScore * 0.15) + (acceptanceScore * 0.10);
        } else if (lowercaseCompany.contains("meta") || lowercaseCompany.contains("facebook")) {
            // Meta loves high-speed recursion/sliding-window, accuracy (acceptance), and streak
            double targetTopicAvg = (strengths.getOrDefault("Recursion & Backtracking", 50) + strengths.getOrDefault("Arrays & Strings", 50)) / 2.0;
            matchScore = (targetTopicAvg * 0.35) + (acceptanceScore * 0.25) + (totalProblemsScore * 0.15) + (streakScore * 0.15) + (mediumPercScore * 0.10);
        } else if (lowercaseCompany.contains("microsoft")) {
            // Microsoft loves sorting/searching, arrays/strings, medium problems
            double targetTopicAvg = (strengths.getOrDefault("Sorting & Searching", 50) + strengths.getOrDefault("Arrays & Strings", 50)) / 2.0;
            matchScore = (targetTopicAvg * 0.35) + (mediumPercScore * 0.25) + (totalProblemsScore * 0.15) + (acceptanceScore * 0.15) + (streakScore * 0.10);
        } else {
            // General formula
            matchScore = (totalProblemsScore * 0.25) + (hardPercScore * 0.20) + (acceptanceScore * 0.15) + (contestScore * 0.15) + (streakScore * 0.10) + (mediumPercScore * 0.15);
        }

        return (int) Math.max(10, Math.min(Math.round(matchScore), 100));
    }

    private void initializeTopicScores(Long userId) {
        List<TopicScores> existing = topicScoresRepository.findByUserId(userId);
        if (!existing.isEmpty()) {
            topicScoresRepository.deleteAll(existing);
        }

        int totalSolved = analyticsService.calculateMetrics(userId).getTotalProblems();
        if (totalSolved == 0) {
            totalSolved = 100;
        }

        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        Object[][] topicData = {
            {"Arrays & Strings", 0.30, 85},
            {"Sorting & Searching", 0.20, 75},
            {"Trees & Graphs", 0.12, 48},
            {"Stacks & Queues", 0.12, 65},
            {"Recursion & Backtracking", 0.10, 55},
            {"Dynamic Programming", 0.08, 35},
            {"Hash Tables", 0.08, 70}
        };

        int runningSum = 0;
        for (int i = 0; i < topicData.length; i++) {
            TopicScores ts = new TopicScores();
            ts.setUserId(userId);
            ts.setTopicName((String) topicData[i][0]);
            double percent = (Double) topicData[i][1];
            
            int solvedCount;
            if (i == topicData.length - 1) {
                solvedCount = totalSolved - runningSum;
            } else {
                solvedCount = (int) Math.round(totalSolved * percent);
                runningSum += solvedCount;
            }
            
            ts.setProblemsSolved(Math.max(1, solvedCount));
            ts.setStrengthScore(new BigDecimal((Integer) topicData[i][2]));
            ts.setLastUpdated(now);
            ts.setCreatedAt(now);
            ts.setUpdatedAt(now);
            topicScoresRepository.save(ts);
        }
    }
}
