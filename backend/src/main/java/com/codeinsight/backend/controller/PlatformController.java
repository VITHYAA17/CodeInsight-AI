package com.codeinsight.backend.controller;

import com.codeinsight.backend.dto.ApiResponse;
import com.codeinsight.backend.dto.ConnectPlatformRequest;
import com.codeinsight.backend.dto.StatisticsDTO;
import com.codeinsight.backend.integration.CodeChefService;
import com.codeinsight.backend.integration.CodeforcesService;
import com.codeinsight.backend.integration.GitHubService;
import com.codeinsight.backend.integration.LeetCodeService;
import com.codeinsight.backend.service.UserService;
import com.codeinsight.backend.util.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/platforms")
public class PlatformController {

    private final UserService userService;
    private final LeetCodeService leetCodeService;
    private final CodeforcesService codeforcesService;
    private final CodeChefService codeChefService;
    private final GitHubService gitHubService;

    public PlatformController(UserService userService,
                            LeetCodeService leetCodeService,
                            CodeforcesService codeforcesService,
                            CodeChefService codeChefService,
                            GitHubService gitHubService) {
        this.userService = userService;
        this.leetCodeService = leetCodeService;
        this.codeforcesService = codeforcesService;
        this.codeChefService = codeChefService;
        this.gitHubService = gitHubService;
    }

    @PostMapping("/connect")
    public ResponseEntity<ApiResponse> connectPlatform(@Valid @RequestBody ConnectPlatformRequest request) {
        String email = SecurityUtil.getCurrentUserEmail();
        Long userId = userService.getUserIdByEmail(email);

        switch (request.getPlatform().toLowerCase()) {
            case "leetcode":
                leetCodeService.connectAccount(userId, request.getUsername());
                break;
            case "codeforces":
                codeforcesService.connectAccount(userId, request.getUsername());
                break;
            case "codechef":
                codeChefService.connectAccount(userId, request.getUsername());
                break;
            case "github":
                gitHubService.connectAccount(userId, request.getUsername());
                break;
            default:
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Invalid platform: " + request.getPlatform()));
        }

        return ResponseEntity.ok(new ApiResponse(true, "Platform connected successfully"));
    }

    @GetMapping("/profile/{platform}")
    public ResponseEntity<?> getPlatformProfile(@PathVariable String platform) {
        String email = SecurityUtil.getCurrentUserEmail();
        Long userId = userService.getUserIdByEmail(email);

        StatisticsDTO stats = null;
        switch (platform.toLowerCase()) {
            case "leetcode":
                stats = leetCodeService.getUserStatistics(userId);
                break;
            case "codeforces":
                stats = codeforcesService.getUserStatistics(userId);
                break;
            case "codechef":
                stats = codeChefService.getUserStatistics(userId);
                break;
            case "github":
                stats = gitHubService.getUserStatistics(userId);
                break;
            default:
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Invalid platform: " + platform));
        }

        if (stats == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(stats);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse> refreshAllPlatforms() {
        String email = SecurityUtil.getCurrentUserEmail();
        Long userId = userService.getUserIdByEmail(email);

        userService.refreshAllPlatforms(userId);

        return ResponseEntity.ok(new ApiResponse(true, "All platforms refreshed successfully"));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCombinedStats() {
        String email = SecurityUtil.getCurrentUserEmail();
        Long userId = userService.getUserIdByEmail(email);

        Map<String, Object> response = new HashMap<>();
        response.put("leetcode", leetCodeService.getUserStatistics(userId));
        response.put("codeforces", codeforcesService.getUserStatistics(userId));
        response.put("codechef", codeChefService.getUserStatistics(userId));
        response.put("github", gitHubService.getUserStatistics(userId));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/accounts")
    public ResponseEntity<?> getConnectedAccounts() {
        String email = SecurityUtil.getCurrentUserEmail();
        Long userId = userService.getUserIdByEmail(email);

        return ResponseEntity.ok(userService.getCodingAccountByUserId(userId));
    }
}
