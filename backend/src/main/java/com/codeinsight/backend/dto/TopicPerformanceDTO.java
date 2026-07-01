package com.codeinsight.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicPerformanceDTO {
    private String topicName;
    private BigDecimal strengthScore;
    private Integer problemsSolved;
}
