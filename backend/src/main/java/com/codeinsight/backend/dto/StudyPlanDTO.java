package com.codeinsight.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudyPlanDTO {
    private Long id;
    private Long userId;
    private Integer weekNumber;
    private String topicName;
    private String taskDescription;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private LocalDateTime updatedAt;
}
