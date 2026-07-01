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
public class InsightsDTO {
    private Integer interviewReadinessScore; // 0-100
    private Map<String, Integer> companyMatchingScores; // Company -> readiness %
    private List<String> topicStrengths; // What to focus on
    private List<SkillGapDTO> skillGaps; // Weak areas to improve
    private String nextMilestone;
    private String performanceLevel; // Beginner, Intermediate, Advanced, Expert
}
