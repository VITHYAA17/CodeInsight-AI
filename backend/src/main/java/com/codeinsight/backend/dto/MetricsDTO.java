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
    private Integer totalProblems;
    private Integer easyCount;
    private Integer mediumCount;
    private Integer hardCount;
    private BigDecimal easyPercentage;
    private BigDecimal mediumPercentage;
    private BigDecimal hardPercentage;
    private BigDecimal averageAcceptanceRate;
    private Integer maxCurrentStreak;
    private Integer averageContestRating;
    private Map<String, PlatformMetricsDTO> platformBreakdown = new HashMap<>();
}
