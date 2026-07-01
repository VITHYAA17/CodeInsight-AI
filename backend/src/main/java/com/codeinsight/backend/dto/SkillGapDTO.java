package com.codeinsight.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillGapDTO {
    private String topic;
    private BigDecimal currentScore;
    private BigDecimal targetScore;
    private Integer estimatedDaysToTarget;
    private String priority; // HIGH, MEDIUM, LOW
}
