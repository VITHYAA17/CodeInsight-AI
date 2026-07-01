package com.codeinsight.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationDTO {
    private Long id;
    private Long userId;
    private String targetCompany;
    private String recommendationText;
    private BigDecimal interviewReadiness;
    private LocalDateTime generatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
