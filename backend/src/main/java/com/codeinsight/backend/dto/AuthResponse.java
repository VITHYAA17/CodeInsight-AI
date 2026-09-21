package com.codeinsight.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String message;
    private UserResponse user;

    public AuthResponse(String token, String message) {
        this.token = token;
        this.message = message;
    }
}