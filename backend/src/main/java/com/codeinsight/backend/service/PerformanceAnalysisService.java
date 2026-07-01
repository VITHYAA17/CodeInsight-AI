package com.codeinsight.backend.service;

import com.codeinsight.backend.dto.PerformanceDTO;
import com.codeinsight.backend.dto.TopicPerformanceDTO;
import com.codeinsight.backend.entity.ContestHistory;
import com.codeinsight.backend.entity.Statistics;
import com.codeinsight.backend.entity.TopicScores;
import com.codeinsight.backend.repository.ContestHistoryRepository;
import com.codeinsight.backend.repository.StatisticsRepository;
import com.codeinsight.backend.repository.TopicScoresRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PerformanceAnalysisService {

    private final TopicScoresRepository topicScoresRepository;
    private final StatisticsRepository statisticsRepository;
    private final ContestHistoryRepository contestHistoryRepository;

    public PerformanceAnalysisService(TopicScoresRepository topicScoresRepository,
                                      StatisticsRepository statisticsRepository,
                                      ContestHistoryRepository contestHistoryRepository) {
        this.topicScoresRepository = topicScoresRepository;
        this.statisticsRepository = statisticsRepository;
        this.contestHistoryRepository = contestHistoryRepository;
    }

    public PerformanceDTO analyzePerformance(Long userId) {
        PerformanceDTO performance = new PerformanceDTO();

        // Get strongest and weakest topics
        List<TopicScores> allTopics = topicScoresRepository.findByUserIdOrderByStrengthScoreDesc(userId);
        
        if (!allTopics.isEmpty()) {
            // Top 3 strongest topics
            List<TopicPerformanceDTO> strongest = allTopics.stream()
                    .limit(3)
                    .map(t -> new TopicPerformanceDTO(t.getTopicName(), t.getStrengthScore(), t.getProblemsSolved()))
                    .collect(Collectors.toList());
            performance.setStrongestTopics(strongest);

            // Top 3 weakest topics
            List<TopicPerformanceDTO> weakest = allTopics.stream()
                    .sorted(Comparator.comparing(TopicScores::getStrengthScore))
                    .limit(3)
                    .map(t -> new TopicPerformanceDTO(t.getTopicName(), t.getStrengthScore(), t.getProblemsSolved()))
                    .collect(Collectors.toList());
            performance.setWeakestTopics(weakest);

            // Improvement areas (topics with score < 50)
            List<String> improvementAreas = allTopics.stream()
                    .filter(t -> t.getStrengthScore().compareTo(new BigDecimal(50)) < 0)
                    .map(TopicScores::getTopicName)
                    .collect(Collectors.toList());
            performance.setImprovementAreas(improvementAreas);
        }

        // Calculate growth trends
        List<Statistics> stats = statisticsRepository.findByUserId(userId);
        if (!stats.isEmpty()) {
            performance.setWeeklyGrowth(calculateWeeklyGrowth(userId));
            performance.setMonthlyGrowth(calculateMonthlyGrowth(userId));
        }

        // Platform comparison
        Map<String, Integer> platformComparison = new HashMap<>();
        for (Statistics stat : stats) {
            platformComparison.put(stat.getPlatform(), stat.getTotalSolved());
        }
        performance.setPlatformComparison(platformComparison);

        // Find strongest platform
        if (!platformComparison.isEmpty()) {
            String strongestPlatform = platformComparison.entrySet().stream()
                    .max(Comparator.comparingInt(Map.Entry::getValue))
                    .map(Map.Entry::getKey)
                    .orElse(null);
            performance.setStrongestPlatform(strongestPlatform);
        }

        // Contest statistics
        List<ContestHistory> contests = contestHistoryRepository.findByUserId(userId);
        performance.setTotalContests(contests.size());

        if (!contests.isEmpty()) {
            int totalRank = 0;
            int validRanks = 0;
            for (ContestHistory contest : contests) {
                if (contest.getRank() != null) {
                    totalRank += contest.getRank();
                    validRanks++;
                }
            }
            if (validRanks > 0) {
                BigDecimal avgRank = new BigDecimal(totalRank).divide(new BigDecimal(validRanks), 2, RoundingMode.HALF_UP);
                performance.setAverageContestRank(avgRank);
            }
        }

        return performance;
    }

    private BigDecimal calculateWeeklyGrowth(Long userId) {
        LocalDate oneWeekAgo = LocalDate.now().minusWeeks(1);
        
        List<ContestHistory> lastWeekContests = contestHistoryRepository
                .findByUserIdAndContestDateBetweenOrderByContestDateDesc(userId, oneWeekAgo, LocalDate.now());
        
        int problemsSolvedThisWeek = lastWeekContests.stream()
                .mapToInt(ContestHistory::getProblemsSolved)
                .sum();

        if (problemsSolvedThisWeek == 0) {
            return BigDecimal.ZERO;
        }

        // Compare with previous week
        LocalDate twoWeeksAgo = LocalDate.now().minusWeeks(2);
        List<ContestHistory> previousWeekContests = contestHistoryRepository
                .findByUserIdAndContestDateBetweenOrderByContestDateDesc(userId, twoWeeksAgo, oneWeekAgo);
        
        int problemsSolvedPreviousWeek = previousWeekContests.stream()
                .mapToInt(ContestHistory::getProblemsSolved)
                .sum();

        if (problemsSolvedPreviousWeek == 0) {
            return new BigDecimal(problemsSolvedThisWeek > 0 ? 100 : 0);
        }

        BigDecimal growth = new BigDecimal(problemsSolvedThisWeek - problemsSolvedPreviousWeek)
                .divide(new BigDecimal(problemsSolvedPreviousWeek), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100));

        return growth;
    }

    private BigDecimal calculateMonthlyGrowth(Long userId) {
        YearMonth currentMonth = YearMonth.now();
        LocalDate startOfCurrentMonth = currentMonth.atDay(1);
        LocalDate endOfCurrentMonth = currentMonth.atEndOfMonth();

        List<ContestHistory> currentMonthContests = contestHistoryRepository
                .findByUserIdAndContestDateBetweenOrderByContestDateDesc(userId, startOfCurrentMonth, endOfCurrentMonth);

        int problemsSolvedThisMonth = currentMonthContests.stream()
                .mapToInt(ContestHistory::getProblemsSolved)
                .sum();

        if (problemsSolvedThisMonth == 0) {
            return BigDecimal.ZERO;
        }

        // Compare with previous month
        YearMonth previousMonth = currentMonth.minusMonths(1);
        LocalDate startOfPreviousMonth = previousMonth.atDay(1);
        LocalDate endOfPreviousMonth = previousMonth.atEndOfMonth();

        List<ContestHistory> previousMonthContests = contestHistoryRepository
                .findByUserIdAndContestDateBetweenOrderByContestDateDesc(userId, startOfPreviousMonth, endOfPreviousMonth);

        int problemsSolvedPreviousMonth = previousMonthContests.stream()
                .mapToInt(ContestHistory::getProblemsSolved)
                .sum();

        if (problemsSolvedPreviousMonth == 0) {
            return new BigDecimal(problemsSolvedThisMonth > 0 ? 100 : 0);
        }

        BigDecimal growth = new BigDecimal(problemsSolvedThisMonth - problemsSolvedPreviousMonth)
                .divide(new BigDecimal(problemsSolvedPreviousMonth), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100));

        return growth;
    }
}
