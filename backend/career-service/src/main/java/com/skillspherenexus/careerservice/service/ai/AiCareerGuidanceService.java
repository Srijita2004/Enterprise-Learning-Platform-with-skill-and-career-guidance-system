package com.skillspherenexus.careerservice.service.ai;

import com.skillspherenexus.careerservice.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiCareerGuidanceService {

    private final VectorSimilarityEngine vectorSimilarityEngine;
    private final GeminiCareerCoachService geminiCareerCoachService;

    public AiCareerEvaluationResponseDTO evaluateCareerReadiness(AiCareerEvaluationRequestDTO request) {
        String employeeName = request.getEmployeeName() != null ? request.getEmployeeName() : "Employee #" + request.getEmployeeId();
        String currentRole = request.getCurrentRole() != null ? request.getCurrentRole() : "Enterprise Associate";
        String targetRole = request.getTargetRole() != null ? request.getTargetRole() : "Senior Professional Specialist";

        // 1. Extract Target Role Benchmark Vector
        Map<String, Integer> benchmark = vectorSimilarityEngine.getTargetRoleBenchmark(targetRole);
        Map<String, Double> targetVector = vectorSimilarityEngine.buildTargetBenchmarkVector(benchmark);

        // 2. Build Employee Feature Vector
        Map<String, Double> employeeVector = vectorSimilarityEngine.buildEmployeeVector(request);

        // 3. Compute Mathematical Cosine Similarity
        double rawCosine = vectorSimilarityEngine.calculateCosineSimilarity(employeeVector, targetVector);
        double matchScore = Math.round(rawCosine * 1000.0) / 10.0; // e.g. 88.5%

        // 4. Compute Skill Gaps
        List<AiSkillGapDTO> skillGaps = vectorSimilarityEngine.calculateSkillGaps(employeeVector, benchmark);

        // 5. Compute Promotion Readiness Probability
        double readinessProb = vectorSimilarityEngine.calculatePromotionReadiness(rawCosine, request, skillGaps.size());
        String readinessTier = readinessProb >= 80.0 ? "HIGH_ADVANCEMENT" :
                readinessProb >= 55.0 ? "MODERATE_PROGRESSION" : "FOUNDATION_BUILDING";

        // 6. Identify Top Strengths
        List<String> strengths = new ArrayList<>();
        if (request.getSkills() != null) {
            strengths = request.getSkills().stream()
                    .filter(s -> s.getProficiencyLevel() != null && s.getProficiencyLevel() >= 3)
                    .map(s -> s.getSkillName() + " (L" + s.getProficiencyLevel() + ")")
                    .limit(4)
                    .collect(Collectors.toList());
        }

        // 7. Recommended Courses from Gaps
        List<String> recommendedCourses = skillGaps.stream()
                .map(AiSkillGapDTO::getTargetCourse)
                .distinct()
                .limit(4)
                .collect(Collectors.toList());

        // 8. Generate Gemini Generative AI Career Guidance Narrative
        Map<String, Object> guidance = geminiCareerCoachService.generateCareerGuidance(
                employeeName, currentRole, targetRole, matchScore, readinessProb, skillGaps, strengths
        );

        String summary = (String) guidance.getOrDefault("executiveSummary", "Career analysis completed successfully.");
        @SuppressWarnings("unchecked")
        List<String> steps = (List<String>) guidance.getOrDefault("strategicSteps", Collections.emptyList());
        String modelEngine = (String) guidance.getOrDefault("engine", "Hybrid ML (Cosine Vector Engine) + Google Gemini GenAI");

        return AiCareerEvaluationResponseDTO.builder()
                .employeeId(request.getEmployeeId())
                .employeeName(employeeName)
                .currentRole(currentRole)
                .targetRole(targetRole)
                .matchScore(matchScore)
                .cosineSimilarity(Math.round(rawCosine * 10000.0) / 10000.0)
                .readinessProbability(readinessProb)
                .readinessTier(readinessTier)
                .skillGaps(skillGaps)
                .topStrengths(strengths)
                .recommendedCourses(recommendedCourses)
                .aiExecutiveSummary(summary)
                .strategicNextSteps(steps)
                .aiModelEngine(modelEngine)
                .generatedAt(Instant.now().toString())
                .build();
    }
}
