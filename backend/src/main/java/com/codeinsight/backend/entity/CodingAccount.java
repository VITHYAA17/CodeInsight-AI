package com.codeinsight.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "coding_account")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodingAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(length = 100)
    private String leetcodeUsername;

    @Column(length = 100)
    private String codeforcesUsername;

    @Column(length = 100)
    private String codechefUsername;

    @Column(length = 100)
    private String geeksforgeeksUsername;

    @Column(length = 100)
    private String githubUsername;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
