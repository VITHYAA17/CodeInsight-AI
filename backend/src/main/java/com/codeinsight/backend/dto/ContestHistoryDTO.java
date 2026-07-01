package com.codeinsight.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContestHistoryDTO {
    private Long id;
    private Long userId;
    private String platform;
    private String contestName;
    private LocalDate contestDate;
    private Integer ratingBefore;
    private Integer ratingAfter;
    private Integer rank;
    private Integer problemsSolved;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
