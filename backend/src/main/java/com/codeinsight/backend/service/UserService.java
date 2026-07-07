package com.codeinsight.backend.service;

import com.codeinsight.backend.dto.ApiResponse;
import com.codeinsight.backend.dto.AuthResponse;
import com.codeinsight.backend.dto.CodingAccountDTO;
import com.codeinsight.backend.dto.LoginRequest;
import com.codeinsight.backend.dto.RegisterRequest;
import com.codeinsight.backend.dto.UserResponse;
import com.codeinsight.backend.dto.UpdateProfileRequest;
import com.codeinsight.backend.entity.CodingAccount;
import com.codeinsight.backend.entity.User;
import com.codeinsight.backend.repository.CodingAccountRepository;
import com.codeinsight.backend.repository.StatisticsRepository;
import com.codeinsight.backend.repository.UserRepository;
import com.codeinsight.backend.security.JwtService;
import com.codeinsight.backend.integration.LeetCodeService;
import com.codeinsight.backend.integration.GeeksforGeeksService;
import com.codeinsight.backend.integration.CodeChefService;
import com.codeinsight.backend.integration.GitHubService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CodingAccountRepository codingAccountRepository;
    private final StatisticsRepository statisticsRepository;
    private final LeetCodeService leetCodeService;
    private final GeeksforGeeksService geeksforGeeksService;
    private final CodeChefService codeChefService;
    private final GitHubService gitHubService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       CodingAccountRepository codingAccountRepository,
                       StatisticsRepository statisticsRepository,
                       LeetCodeService leetCodeService,
                       GeeksforGeeksService geeksforGeeksService,
                       CodeChefService codeChefService,
                       GitHubService gitHubService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.codingAccountRepository = codingAccountRepository;
        this.statisticsRepository = statisticsRepository;
        this.leetCodeService = leetCodeService;
        this.geeksforGeeksService = geeksforGeeksService;
        this.codeChefService = codeChefService;
        this.gitHubService = gitHubService;
    }

    public ApiResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return new ApiResponse(false, "Email is already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail());
        return new ApiResponse(true, "Registration successful", new AuthResponse(token, "Registration successful"));
    }

    public ApiResponse login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isEmpty() || !passwordEncoder.matches(request.getPassword(), userOpt.get().getPassword())) {
            return new ApiResponse(false, "Invalid email or password");
        }

        User user = userOpt.get();
        String token = jwtService.generateToken(user.getEmail());
        return new ApiResponse(true, "Login successful", new AuthResponse(token, "Login successful"));
    }

    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToUserResponse(user);
    }

    public Long getUserIdByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getName() != null) user.setName(request.getName());
        if (request.getCollegeName() != null) user.setCollegeName(request.getCollegeName());
        if (request.getResumeUrl() != null) user.setResumeUrl(request.getResumeUrl());
        if (request.getProfilePhoto() != null) user.setProfilePhoto(request.getProfilePhoto());
        if (request.getContactNumber() != null) user.setContactNumber(request.getContactNumber());

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return mapToUserResponse(user);
    }

    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getCollegeName(),
                user.getResumeUrl(),
                user.getProfilePhoto(),
                user.getContactNumber()
        );
    }

    public CodingAccountDTO getCodingAccountByUserId(Long userId) {
        Optional<CodingAccount> account = codingAccountRepository.findByUserId(userId);
        return account.map(this::mapToCodingAccountDTO).orElse(null);
    }

    public void refreshAllPlatforms(Long userId) {
        Optional<CodingAccount> account = codingAccountRepository.findByUserId(userId);
        
        if (account.isPresent()) {
            CodingAccount ca = account.get();
            
            if (ca.getLeetcodeUsername() != null) {
                leetCodeService.syncUserData(userId, ca.getLeetcodeUsername());
            }
            if (ca.getGeeksforgeeksUsername() != null) {
                geeksforGeeksService.syncUserData(userId, ca.getGeeksforgeeksUsername());
            }
            if (ca.getCodechefUsername() != null) {
                codeChefService.syncUserData(userId, ca.getCodechefUsername());
            }
            if (ca.getGithubUsername() != null) {
                gitHubService.syncUserData(userId, ca.getGithubUsername());
            }
        }
    }

    private CodingAccountDTO mapToCodingAccountDTO(CodingAccount account) {
        return new CodingAccountDTO(
                account.getId(),
                account.getUserId(),
                account.getLeetcodeUsername(),
                account.getCodeforcesUsername(),
                account.getCodechefUsername(),
                account.getGeeksforgeeksUsername(),
                account.getGithubUsername(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}