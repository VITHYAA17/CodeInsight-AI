package com.codeinsight.backend.controller;

import com.codeinsight.backend.ai.RecommendationGeneratorService;
import com.codeinsight.backend.ai.StudyPlanGeneratorService;
import com.codeinsight.backend.dto.ApiResponse;
import com.codeinsight.backend.dto.RecommendationDTO;
import com.codeinsight.backend.entity.Recommendation;
import com.codeinsight.backend.repository.RecommendationRepository;
import com.codeinsight.backend.repository.StudyPlanRepository;
import com.codeinsight.backend.entity.StudyPlan;
import com.codeinsight.backend.entity.Recommendation;
import com.codeinsight.backend.security.SecurityUtil;
import com.codeinsight.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI Services", description = "AI-powered recommendations and study plans")
public class AiController {

    private final RecommendationGeneratorService recommendationGeneratorService;
    private final StudyPlanGeneratorService studyPlanGeneratorService;
    private final UserService userService;
    private final StudyPlanRepository studyPlanRepository;
    private final RecommendationRepository recommendationRepository;

    public AiController(RecommendationGeneratorService recommendationGeneratorService,
                       StudyPlanGeneratorService studyPlanGeneratorService,
                       UserService userService,
                       StudyPlanRepository studyPlanRepository,
                       RecommendationRepository recommendationRepository) {
        this.recommendationGeneratorService = recommendationGeneratorService;
        this.studyPlanGeneratorService = studyPlanGeneratorService;
        this.userService = userService;
        this.studyPlanRepository = studyPlanRepository;
        this.recommendationRepository = recommendationRepository;
    }

    /**
     * Generate AI-powered interview recommendations for a target company
     */
    @PostMapping("/generate-recommendations")
    @Operation(summary = "Generate interview recommendations", 
              description = "Generate AI-powered recommendations for target company interview preparation")
    public ResponseEntity<ApiResponse> generateRecommendations(
            @RequestParam String targetCompany) {
        try {
            String userEmail = SecurityUtil.getCurrentUserEmail();
            Long userId = userService.getUserByEmail(userEmail).getId();

            Recommendation recommendation = recommendationGeneratorService.generateRecommendations(userId, targetCompany);

            RecommendationDTO dto = new RecommendationDTO(
                    recommendation.getId(),
                    recommendation.getUser().getId(),
                    recommendation.getTargetCompany(),
                    recommendation.getRecommendationText(),
                    recommendation.getInterviewReadiness(),
                    recommendation.getGeneratedAt()
            );

            return ResponseEntity.ok(new ApiResponse(
                    true,
                    "Recommendations generated successfully for " + targetCompany,
                    dto
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(
                    false,
                    "Failed to generate recommendations: " + e.getMessage(),
                    null
            ));
        }
    }

    /**
     * Generate AI-powered week-by-week study plan for interview preparation
     */
    @PostMapping("/generate-study-plan")
    @Operation(summary = "Generate study plan", 
              description = "Generate AI-powered week-by-week study plan for interview preparation")
    public ResponseEntity<ApiResponse> generateStudyPlan(
            @RequestParam String targetCompany,
            @RequestParam Integer weeksAvailable) {
        try {
            String userEmail = SecurityUtil.getCurrentUserEmail();
            Long userId = userService.getUserByEmail(userEmail).getId();

            Map<String, Object> planSummary = studyPlanGeneratorService.generateStudyPlan(
                    userId, targetCompany, weeksAvailable
            );

            return ResponseEntity.ok(new ApiResponse(
                    true,
                    (String) planSummary.get("message"),
                    planSummary
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(
                    false,
                    "Invalid parameters: " + e.getMessage(),
                    null
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(
                    false,
                    "Failed to generate study plan: " + e.getMessage(),
                    null
            ));
        }
    }

    @GetMapping("/study-plan")
    @Operation(summary = "Get study plan", description = "Get study plan tasks for the current user")
    public ResponseEntity<ApiResponse> getStudyPlan() {
        try {
            String userEmail = SecurityUtil.getCurrentUserEmail();
            Long userId = userService.getUserByEmail(userEmail).getId();
            List<StudyPlan> plans = studyPlanRepository.findByUserIdOrderByWeekNumberAsc(userId);
            return ResponseEntity.ok(new ApiResponse(true, "Study plan fetched successfully", plans));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PutMapping("/study-plan/{id}/toggle")
    @Operation(summary = "Toggle study plan task", description = "Toggle standard study task status between PENDING and COMPLETED")
    public ResponseEntity<ApiResponse> toggleStudyPlanTask(@PathVariable Long id) {
        try {
            String userEmail = SecurityUtil.getCurrentUserEmail();
            Long userId = userService.getUserByEmail(userEmail).getId();

            StudyPlan plan = studyPlanRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Study plan task not found"));

            if (!plan.getUserId().equals(userId)) {
                return ResponseEntity.status(403).body(new ApiResponse(false, "Unauthorized to access this study plan task"));
            }

            if ("COMPLETED".equals(plan.getStatus())) {
                plan.setStatus("PENDING");
                plan.setCompletedAt(null);
            } else {
                plan.setStatus("COMPLETED");
                plan.setCompletedAt(LocalDateTime.now());
            }
            plan.setUpdatedAt(LocalDateTime.now());
            studyPlanRepository.save(plan);

            return ResponseEntity.ok(new ApiResponse(true, "Task status toggled successfully", plan));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/recommendations")
    @Operation(summary = "Get recommendations", description = "Get generated interview recommendations for the current user")
    public ResponseEntity<ApiResponse> getRecommendations() {
        try {
            String userEmail = SecurityUtil.getCurrentUserEmail();
            Long userId = userService.getUserByEmail(userEmail).getId();
            List<Recommendation> recommendations = recommendationRepository.findByUserIdOrderByGeneratedAtDesc(userId);

            List<RecommendationDTO> dtos = recommendations.stream()
                    .map(r -> new RecommendationDTO(
                            r.getId(),
                            r.getUser().getId(),
                            r.getTargetCompany(),
                            r.getRecommendationText(),
                            r.getInterviewReadiness(),
                            r.getGeneratedAt()
                    ))
                    .toList();

            return ResponseEntity.ok(new ApiResponse(true, "Recommendations fetched successfully", dtos));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
}
