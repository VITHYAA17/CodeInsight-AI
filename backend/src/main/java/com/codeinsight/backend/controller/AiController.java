package com.codeinsight.backend.controller;

import com.codeinsight.backend.ai.RecommendationGeneratorService;
import com.codeinsight.backend.ai.StudyPlanGeneratorService;
import com.codeinsight.backend.dto.ApiResponse;
import com.codeinsight.backend.dto.RecommendationDTO;
import com.codeinsight.backend.entity.Recommendation;
import com.codeinsight.backend.security.SecurityUtil;
import com.codeinsight.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI Services", description = "AI-powered recommendations and study plans")
public class AiController {

    private final RecommendationGeneratorService recommendationGeneratorService;
    private final StudyPlanGeneratorService studyPlanGeneratorService;
    private final UserService userService;

    public AiController(RecommendationGeneratorService recommendationGeneratorService,
                       StudyPlanGeneratorService studyPlanGeneratorService,
                       UserService userService) {
        this.recommendationGeneratorService = recommendationGeneratorService;
        this.studyPlanGeneratorService = studyPlanGeneratorService;
        this.userService = userService;
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
}
