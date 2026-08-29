package com.skillspherenexus.careerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiCareerEvaluationResponseDTO {
    private Integer employeeId;
    private String employeeName;
    private String currentRole;
    private String targetRole;
    
    // Mathematical ML Metrics
    private Double matchScore;             // e.g. 88.5% (Cosine Similarity scaled)
    private Double cosineSimilarity;        // raw cosine similarity 0.0 - 1.0
    private Double readinessProbability;   // e.g. 82.0%
    private String readinessTier;          // HIGH_ADVANCEMENT, MODERATE_PROGRESSION, FOUNDATION_BUILDING
    
    // Skill Gap Breakdown
    private List<AiSkillGapDTO> skillGaps;
    private List<String> topStrengths;
    private List<String> recommendedCourses;
    
    // Generative AI / Gemini Career Guidance
    private String aiExecutiveSummary;
    private List<String> strategicNextSteps;
    private String aiModelEngine;          // "Hybrid ML (Cosine Vector Engine) + Google Gemini GenAI"
    private String generatedAt;
}
