package com.codeinsight.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectPlatformRequest {
    private String platform; // leetcode, codeforces, codechef, github
    private String username;
}
