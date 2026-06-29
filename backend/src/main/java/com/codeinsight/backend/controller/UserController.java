package com.codeinsight.backend.controller;
import jakarta.validation.Valid;
import com.codeinsight.backend.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import com.codeinsight.backend.dto.RegisterRequest;
import com.codeinsight.backend.service.UserService;



import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
    return ResponseEntity.ok(userService.register(request));
}
}
