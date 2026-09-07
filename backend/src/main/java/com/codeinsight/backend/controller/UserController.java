package com.codeinsight.backend.controller;

import com.codeinsight.backend.dto.ApiResponse;
import com.codeinsight.backend.dto.LoginRequest;
import com.codeinsight.backend.dto.RegisterRequest;
import com.codeinsight.backend.dto.UserResponse;
import com.codeinsight.backend.dto.UpdateProfileRequest;
import com.codeinsight.backend.service.UserService;
import com.codeinsight.backend.security.SecurityUtil;
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
        ApiResponse response = service.register(request);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request) {
        ApiResponse response = service.login(request);
        if (!response.isSuccess()) {
            return ResponseEntity.status(401).body(response);
        }
        return ResponseEntity.ok(response);
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

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }
}