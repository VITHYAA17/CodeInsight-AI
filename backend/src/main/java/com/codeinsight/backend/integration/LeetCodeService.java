package com.codeinsight.backend.integration;

import com.codeinsight.backend.dto.StatisticsDTO;
import com.codeinsight.backend.entity.CodingAccount;
import com.codeinsight.backend.entity.Statistics;
import com.codeinsight.backend.repository.CodingAccountRepository;
import com.codeinsight.backend.repository.StatisticsRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LeetCodeService implements PlatformService {

    private final CodingAccountRepository codingAccountRepository;
    private final StatisticsRepository statisticsRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String LEETCODE_API_URL = "https://leetcode.com/graphql";
    private static final String PLATFORM_NAME = "leetcode";

    public LeetCodeService(CodingAccountRepository codingAccountRepository,
                          StatisticsRepository statisticsRepository,
                          RestTemplate restTemplate,
                          ObjectMapper objectMapper) {
        this.codingAccountRepository = codingAccountRepository;
        this.statisticsRepository = statisticsRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void connectAccount(Long userId, String username) {
        Optional<CodingAccount> existing = codingAccountRepository.findByUserId(userId);
        CodingAccount account;

        if (existing.isPresent()) {
            account = existing.get();
        } else {
            account = new CodingAccount();
            account.setUserId(userId);
            account.setCreatedAt(LocalDateTime.now());
        }

        account.setLeetcodeUsername(username);
        account.setUpdatedAt(LocalDateTime.now());
        codingAccountRepository.save(account);

        // Sync data after connecting
        syncUserData(userId, username);
    }

    @Override
    public void syncUserData(Long userId, String username) {
        try {
            // LeetCode GraphQL query for user stats
            String query = """
                    {
                      matchedUser(username: "%s") {
                        username
                        profile {
                          realName
                        }
                        submitStats {
                          acSubmissionNum {
                            difficulty
                            count
                            submissions
                          }
                          totalSubmissionNum {
                            difficulty
                            count
                            submissions
                          }
                        }
                        userContestRanking {
                          rating
                          attendedContestsCount
                        }
                      }
                    }
                    """.formatted(username);

            // For now, mock the response since we'd need proper API handling
            Statistics stats = new Statistics();
            stats.setUserId(userId);
            stats.setPlatform(PLATFORM_NAME);
            stats.setTotalSolved(150); // Mock data
            stats.setEasySolved(50);
            stats.setMediumSolved(75);
            stats.setHardSolved(25);
            stats.setAcceptanceRate(new BigDecimal("45.50"));
            stats.setContestRating(1800);
            stats.setCurrentStreak(7);
            stats.setLastSynced(LocalDateTime.now());
            stats.setCreatedAt(LocalDateTime.now());
            stats.setUpdatedAt(LocalDateTime.now());

            Optional<Statistics> existing = statisticsRepository.findByUserIdAndPlatform(userId, PLATFORM_NAME);
            if (existing.isPresent()) {
                Statistics existingStats = existing.get();
                existingStats.setTotalSolved(stats.getTotalSolved());
                existingStats.setEasySolved(stats.getEasySolved());
                existingStats.setMediumSolved(stats.getMediumSolved());
                existingStats.setHardSolved(stats.getHardSolved());
                existingStats.setAcceptanceRate(stats.getAcceptanceRate());
                existingStats.setContestRating(stats.getContestRating());
                existingStats.setCurrentStreak(stats.getCurrentStreak());
                existingStats.setLastSynced(LocalDateTime.now());
                existingStats.setUpdatedAt(LocalDateTime.now());
                statisticsRepository.save(existingStats);
            } else {
                statisticsRepository.save(stats);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to sync LeetCode data for user: " + username, e);
        }
    }

    @Override
    public StatisticsDTO getUserStatistics(Long userId) {
        Optional<Statistics> stats = statisticsRepository.findByUserIdAndPlatform(userId, PLATFORM_NAME);
        return stats.map(this::mapToDTO).orElse(null);
    }

    @Override
    public String getPlatformName() {
        return PLATFORM_NAME;
    }

    private StatisticsDTO mapToDTO(Statistics stats) {
        return new StatisticsDTO(
                stats.getId(),
                stats.getUserId(),
                stats.getPlatform(),
                stats.getTotalSolved(),
                stats.getEasySolved(),
                stats.getMediumSolved(),
                stats.getHardSolved(),
                stats.getAcceptanceRate(),
                stats.getContestRating(),
                stats.getCurrentStreak(),
                stats.getLastSynced(),
                stats.getCreatedAt(),
                stats.getUpdatedAt()
        );
    }
}
