package com.codeinsight.backend.integration;

import com.codeinsight.backend.dto.StatisticsDTO;
import com.codeinsight.backend.entity.CodingAccount;
import com.codeinsight.backend.entity.Statistics;
import com.codeinsight.backend.repository.CodingAccountRepository;
import com.codeinsight.backend.repository.StatisticsRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestTemplate;

@Service
public class CodeforcesService implements PlatformService {

    private final CodingAccountRepository codingAccountRepository;
    private final StatisticsRepository statisticsRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String CODEFORCES_API_URL = "https://codeforces.com/api";
    private static final String PLATFORM_NAME = "codeforces";

    public CodeforcesService(CodingAccountRepository codingAccountRepository,
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

        account.setCodeforcesUsername(username);
        account.setUpdatedAt(LocalDateTime.now());
        codingAccountRepository.save(account);

        syncUserData(userId, username);
    }

    @Override
    public void syncUserData(Long userId, String username) {
        try {
            // Get user info
            String infoUrl = CODEFORCES_API_URL + "/user.info?handles=" + username;
            String infoResponse = restTemplate.getForObject(infoUrl, String.class);
            JsonNode infoRoot = objectMapper.readTree(infoResponse);
            
            if (!"OK".equalsIgnoreCase(infoRoot.path("status").asText())) {
                throw new RuntimeException("Codeforces user info API call failed");
            }
            
            JsonNode userObj = infoRoot.path("result").path(0);
            if (userObj.isMissingNode() || userObj.isNull()) {
                throw new RuntimeException("User not found on Codeforces: " + username);
            }
            
            int rating = userObj.path("rating").asInt(0);
            
            // Get user submissions
            String statusUrl = CODEFORCES_API_URL + "/user.status?handle=" + username;
            String statusResponse = restTemplate.getForObject(statusUrl, String.class);
            JsonNode statusRoot = objectMapper.readTree(statusResponse);
            
            if (!"OK".equalsIgnoreCase(statusRoot.path("status").asText())) {
                throw new RuntimeException("Codeforces user status API call failed");
            }
            
            Set<String> solvedProblems = new HashSet<>();
            int easySolved = 0;
            int mediumSolved = 0;
            int hardSolved = 0;
            
            JsonNode subNode = statusRoot.path("result");
            if (subNode.isArray()) {
                for (JsonNode sub : subNode) {
                    if ("OK".equalsIgnoreCase(sub.path("verdict").asText())) {
                        JsonNode problem = sub.path("problem");
                        String problemKey = problem.path("contestId").asInt(0) + "_" + problem.path("index").asText();
                        if (solvedProblems.add(problemKey)) {
                            int problemRating = problem.path("rating").asInt(0);
                            if (problemRating < 1200) {
                                easySolved++;
                            } else if (problemRating < 1900) {
                                mediumSolved++;
                            } else {
                                hardSolved++;
                            }
                        }
                    }
                }
            }
            
            int totalSolved = solvedProblems.size();
            double totalSubmissions = subNode.isArray() ? subNode.size() : 1.0;
            BigDecimal acceptanceRate = BigDecimal.ZERO;
            if (totalSubmissions > 0) {
                double rate = (totalSolved / totalSubmissions) * 100.0;
                acceptanceRate = new BigDecimal(rate).setScale(2, java.math.RoundingMode.HALF_UP);
            }
            
            Statistics stats = new Statistics();
            stats.setUserId(userId);
            stats.setPlatform(PLATFORM_NAME);
            stats.setTotalSolved(totalSolved);
            stats.setEasySolved(easySolved);
            stats.setMediumSolved(mediumSolved);
            stats.setHardSolved(hardSolved);
            stats.setAcceptanceRate(acceptanceRate);
            stats.setContestRating(rating);
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
                existingStats.setLastSynced(LocalDateTime.now());
                existingStats.setUpdatedAt(LocalDateTime.now());
                statisticsRepository.save(existingStats);
            } else {
                statisticsRepository.save(stats);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to sync Codeforces data for user: " + username, e);
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
