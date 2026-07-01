package com.codeinsight.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsDTO {
    private Long id;
    private Long userId;
    private String platform;
    private Integer totalSolved;
    private Integer easySolved;
    private Integer mediumSolved;
    private Integer hardSolved;
    private BigDecimal acceptanceRate;
    private Integer contestRating;
    private Integer currentStreak;
    private LocalDateTime lastSynced;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
