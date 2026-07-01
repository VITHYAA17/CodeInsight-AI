package com.codeinsight.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contest_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContestHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String platform; // LeetCode, Codeforces, CodeChef

    @Column(nullable = false, length = 255)
    private String contestName;

    @Column(nullable = false)
    private LocalDate contestDate;

    @Column
    private Integer ratingBefore;

    @Column
    private Integer ratingAfter;

    @Column
    private Integer rank;

    @Column(nullable = false)
    private Integer problemsSolved = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
