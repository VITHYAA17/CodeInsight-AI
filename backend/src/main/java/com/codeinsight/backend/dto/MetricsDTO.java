package com.codeinsight.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricsDTO {
    private Integer totalProblems = 0;
    private Integer easyCount = 0;
    private Integer mediumCount = 0;
    private Integer hardCount = 0;
    private BigDecimal easyPercentage = BigDecimal.ZERO;
    private BigDecimal mediumPercentage = BigDecimal.ZERO;
    private BigDecimal hardPercentage = BigDecimal.ZERO;
    private BigDecimal averageAcceptanceRate = BigDecimal.ZERO;
    private Integer maxCurrentStreak = 0;
    private Integer averageContestRating = 0;
    private Map<String, PlatformMetricsDTO> platformBreakdown = new HashMap<>();
}
