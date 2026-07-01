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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Service
public class GitHubService implements PlatformService {

    private final CodingAccountRepository codingAccountRepository;
    private final StatisticsRepository statisticsRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String GITHUB_API_URL = "https://api.github.com";
    private static final String PLATFORM_NAME = "github";

    public GitHubService(CodingAccountRepository codingAccountRepository,
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

        account.setGithubUsername(username);
        account.setUpdatedAt(LocalDateTime.now());
        codingAccountRepository.save(account);

        syncUserData(userId, username);
    }

    @Override
    public void syncUserData(Long userId, String username) {
        try {
            String url = GITHUB_API_URL + "/users/" + username;
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            
            if (rootNode.isMissingNode() || rootNode.isNull()) {
                throw new RuntimeException("User not found on GitHub: " + username);
            }
            
            int publicRepos = rootNode.path("public_repos").asInt(0);
            
            int totalSolved = publicRepos;
            int easySolved = (int) Math.round(totalSolved * 0.3);
            int mediumSolved = (int) Math.round(totalSolved * 0.5);
            int hardSolved = totalSolved - (easySolved + mediumSolved);
            
            Statistics stats = new Statistics();
            stats.setUserId(userId);
            stats.setPlatform(PLATFORM_NAME);
            stats.setTotalSolved(totalSolved);
            stats.setEasySolved(easySolved);
            stats.setMediumSolved(mediumSolved);
            stats.setHardSolved(hardSolved);
            stats.setAcceptanceRate(new BigDecimal("100.00"));
            stats.setContestRating(0); // GitHub doesn't have ratings
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
                existingStats.setLastSynced(LocalDateTime.now());
                existingStats.setUpdatedAt(LocalDateTime.now());
                statisticsRepository.save(existingStats);
            } else {
                statisticsRepository.save(stats);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to sync GitHub data for user: " + username, e);
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
