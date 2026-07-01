package com.codeinsight.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "study_plan")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudyPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer weekNumber;

    @Column(nullable = false, length = 100)
    private String topicName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String taskDescription;

    @Column(nullable = false, length = 20)
    private String status = "PENDING"; // PENDING, IN_PROGRESS, COMPLETED

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
