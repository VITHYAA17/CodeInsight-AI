package com.codeinsight.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodingAccountDTO {
    private Long id;
    private Long userId;
    private String leetcodeUsername;
    private String codeforcesUsername;
    private String codechefUsername;
    private String geeksforgeeksUsername;
    private String githubUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
