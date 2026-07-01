package com.codeinsight.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "statistics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Statistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String platform; // leetcode, codeforces, codechef, gfg, github

    @Column(nullable = false)
    private Integer totalSolved = 0;

    @Column(nullable = false)
    private Integer easySolved = 0;

    @Column(nullable = false)
    private Integer mediumSolved = 0;

    @Column(nullable = false)
    private Integer hardSolved = 0;

    @Column(precision = 5, scale = 2)
    private BigDecimal acceptanceRate;

    @Column
    private Integer contestRating;

    @Column(nullable = false)
    private Integer currentStreak = 0;

    @Column(name = "last_synced", nullable = false)
    private LocalDateTime lastSynced;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
