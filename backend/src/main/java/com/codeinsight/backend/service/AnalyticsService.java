package com.codeinsight.backend.service;

import com.codeinsight.backend.dto.MetricsDTO;
import com.codeinsight.backend.dto.PlatformMetricsDTO;
import com.codeinsight.backend.entity.Statistics;
import com.codeinsight.backend.repository.StatisticsRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class AnalyticsService {

    private final StatisticsRepository statisticsRepository;

    public AnalyticsService(StatisticsRepository statisticsRepository) {
        this.statisticsRepository = statisticsRepository;
    }

    public MetricsDTO calculateMetrics(Long userId) {
        List<Statistics> allStats = statisticsRepository.findByUserId(userId);

        if (allStats.isEmpty()) {
            return new MetricsDTO();
        }

        MetricsDTO metrics = new MetricsDTO();

        // Calculate total counts
        int totalProblems = 0;
        int totalEasy = 0;
        int totalMedium = 0;
        int totalHard = 0;
        BigDecimal totalAcceptance = BigDecimal.ZERO;
        int totalRating = 0;
        int maxStreak = 0;
        int platformCount = 0;

        // Process each platform's stats
        for (Statistics stat : allStats) {
            totalProblems += stat.getTotalSolved();
            totalEasy += stat.getEasySolved();
            totalMedium += stat.getMediumSolved();
            totalHard += stat.getHardSolved();

            if (stat.getAcceptanceRate() != null) {
                totalAcceptance = totalAcceptance.add(stat.getAcceptanceRate());
                platformCount++;
            }

            if (stat.getContestRating() != null) {
                totalRating += stat.getContestRating();
            }

            if (stat.getCurrentStreak() != null) {
                maxStreak = Math.max(maxStreak, stat.getCurrentStreak());
            }

            // Add to platform breakdown
            PlatformMetricsDTO platformMetric = new PlatformMetricsDTO(
                    stat.getPlatform(),
                    stat.getTotalSolved(),
                    stat.getEasySolved(),
                    stat.getMediumSolved(),
                    stat.getHardSolved(),
                    stat.getAcceptanceRate(),
                    stat.getContestRating(),
                    stat.getCurrentStreak()
            );
            metrics.getPlatformBreakdown().put(stat.getPlatform(), platformMetric);
        }

        // Set total counts
        metrics.setTotalProblems(totalProblems);
        metrics.setEasyCount(totalEasy);
        metrics.setMediumCount(totalMedium);
        metrics.setHardCount(totalHard);
        metrics.setCurrentStreak(maxStreak);

        // Calculate percentages
        if (totalProblems > 0) {
            BigDecimal total = new BigDecimal(totalProblems);
            metrics.setEasyPercentage(
                    new BigDecimal(totalEasy).divide(total, 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100))
            );
            metrics.setMediumPercentage(
                    new BigDecimal(totalMedium).divide(total, 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100))
            );
            metrics.setHardPercentage(
                    new BigDecimal(totalHard).divide(total, 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100))
            );
        }

        // Calculate average acceptance rate
        if (platformCount > 0) {
            metrics.setAverageAcceptanceRate(
                    totalAcceptance.divide(new BigDecimal(platformCount), 2, RoundingMode.HALF_UP)
            );
        }

        // Calculate average contest rating
        if (!allStats.isEmpty()) {
            int nonNullRatings = 0;
            for (Statistics stat : allStats) {
                if (stat.getContestRating() != null) {
                    nonNullRatings++;
                }
            }
            if (nonNullRatings > 0) {
                metrics.setAverageContestRating(totalRating / nonNullRatings);
            }
        }

        return metrics;
    }
}
