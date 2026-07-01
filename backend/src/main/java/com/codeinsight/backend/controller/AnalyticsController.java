package com.codeinsight.backend.controller;

import com.codeinsight.backend.dto.MetricsDTO;
import com.codeinsight.backend.dto.PerformanceDTO;
import com.codeinsight.backend.service.AnalyticsService;
import com.codeinsight.backend.service.PerformanceAnalysisService;
import com.codeinsight.backend.service.UserService;
import com.codeinsight.backend.util.SecurityUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final PerformanceAnalysisService performanceAnalysisService;
    private final UserService userService;

    public AnalyticsController(AnalyticsService analyticsService,
                             PerformanceAnalysisService performanceAnalysisService,
                             UserService userService) {
        this.analyticsService = analyticsService;
        this.performanceAnalysisService = performanceAnalysisService;
        this.userService = userService;
    }

    @GetMapping("/metrics")
    public ResponseEntity<MetricsDTO> getMetrics() {
        String email = SecurityUtil.getCurrentUserEmail();
        Long userId = userService.getUserIdByEmail(email);
        
        MetricsDTO metrics = analyticsService.calculateMetrics(userId);
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/performance")
    public ResponseEntity<PerformanceDTO> getPerformanceAnalysis() {
        String email = SecurityUtil.getCurrentUserEmail();
        Long userId = userService.getUserIdByEmail(email);
        
        PerformanceDTO performance = performanceAnalysisService.analyzePerformance(userId);
        return ResponseEntity.ok(performance);
    }
}
