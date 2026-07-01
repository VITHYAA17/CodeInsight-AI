package com.codeinsight.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceDTO {
    private List<TopicPerformanceDTO> strongestTopics;
    private List<TopicPerformanceDTO> weakestTopics;
    private BigDecimal weeklyGrowth;
    private BigDecimal monthlyGrowth;
    private Map<String, Integer> platformComparison;
    private List<String> improvementAreas;
    private String strongestPlatform;
    private Integer totalContests;
    private BigDecimal averageContestRank;
}
