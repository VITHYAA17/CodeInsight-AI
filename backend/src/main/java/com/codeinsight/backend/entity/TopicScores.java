package com.codeinsight.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "topic_scores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicScores {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String topicName; // Arrays, Graphs, DP, Trees, Strings, etc.

    @Column(nullable = false)
    private Integer problemsSolved = 0;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal strengthScore = new BigDecimal("0.00"); // 0-100

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
