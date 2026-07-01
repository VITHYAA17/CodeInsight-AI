package com.codeinsight.backend.controller;

import com.codeinsight.backend.dto.ApiResponse;
import com.codeinsight.backend.dto.AuthResponse;
import com.codeinsight.backend.dto.LoginRequest;
import com.codeinsight.backend.dto.RegisterRequest;
import com.codeinsight.backend.dto.UserResponse;
import com.codeinsight.backend.dto.UpdateProfileRequest;
import com.codeinsight.backend.service.UserService;
import com.codeinsight.backend.util.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(service.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getCurrentUser() {
        String email = SecurityUtil.getCurrentUserEmail();
        UserResponse user = service.getUserByEmail(email);
        return ResponseEntity.ok(new ApiResponse(true, "User profile fetched", user));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        String email = SecurityUtil.getCurrentUserEmail();
        UserResponse updated = service.updateProfile(email, request);
        return ResponseEntity.ok(new ApiResponse(true, "Profile updated successfully", updated));
    }
}