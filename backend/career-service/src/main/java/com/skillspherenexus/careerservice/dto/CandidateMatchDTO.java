package com.skillspherenexus.careerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateMatchDTO {
    private Long employeeId;
    private Long careerPlanId;
    private String employeeName;
    private String currentRole;
    private String targetRole;
    private Integer matchScore;
    private Integer progressPercentage;
    private String mentorName;
    private List<String> matchingSkills;
    private List<String> missingSkills;
    private String suitabilityTier;
}
