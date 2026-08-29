package com.skillspherenexus.careerservice.controller;

import com.skillspherenexus.careerservice.dto.AiCareerEvaluationRequestDTO;
import com.skillspherenexus.careerservice.dto.AiCareerEvaluationResponseDTO;
import com.skillspherenexus.careerservice.service.ai.AiCareerGuidanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/career/ai")
@RequiredArgsConstructor
public class AiCareerController {

    private final AiCareerGuidanceService aiCareerGuidanceService;

    @PreAuthorize("hasAnyRole('ADMIN','HR','EMPLOYEE','LEARNER')")
    @PostMapping("/evaluate")
    public ResponseEntity<AiCareerEvaluationResponseDTO> evaluateCareerReadiness(
            @RequestBody AiCareerEvaluationRequestDTO request
    ) {
        AiCareerEvaluationResponseDTO response = aiCareerGuidanceService.evaluateCareerReadiness(request);
        return ResponseEntity.ok(response);
    }
}
