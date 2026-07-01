package com.codeinsight.backend.integration;

import com.codeinsight.backend.dto.StatisticsDTO;
import com.codeinsight.backend.entity.CodingAccount;
import com.codeinsight.backend.entity.Statistics;
import com.codeinsight.backend.repository.CodingAccountRepository;
import com.codeinsight.backend.repository.StatisticsRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CodeChefService implements PlatformService {

    private final CodingAccountRepository codingAccountRepository;
    private final StatisticsRepository statisticsRepository;

    private static final String CODECHEF_API_URL = "https://www.codechef.com/api/v2";
    private static final String PLATFORM_NAME = "codechef";

    public CodeChefService(CodingAccountRepository codingAccountRepository,
                          StatisticsRepository statisticsRepository) {
        this.codingAccountRepository = codingAccountRepository;
        this.statisticsRepository = statisticsRepository;
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

        account.setCodechefUsername(username);
        account.setUpdatedAt(LocalDateTime.now());
        codingAccountRepository.save(account);

        syncUserData(userId, username);
    }

    @Override
    public void syncUserData(Long userId, String username) {
        try {
            // API endpoint: https://www.codechef.com/api/v2/users/username
            Statistics stats = new Statistics();
            stats.setUserId(userId);
            stats.setPlatform(PLATFORM_NAME);
            stats.setTotalSolved(95); // Mock data
            stats.setEasySolved(35);
            stats.setMediumSolved(45);
            stats.setHardSolved(15);
            stats.setAcceptanceRate(new BigDecimal("48.75"));
            stats.setContestRating(1550);
            stats.setCurrentStreak(0);
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
            throw new RuntimeException("Failed to sync CodeChef data for user: " + username, e);
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
