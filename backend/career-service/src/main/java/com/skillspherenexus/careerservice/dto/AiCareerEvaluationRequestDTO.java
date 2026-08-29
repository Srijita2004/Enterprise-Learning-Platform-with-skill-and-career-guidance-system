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
public class AiCareerEvaluationRequestDTO {
    private Integer employeeId;
    private String employeeName;
    private String currentRole;
    private String targetRole;
    private List<SkillInput> skills;
    private List<AssessmentInput> assessments;
    private List<String> certifications;
    private Integer yearsExperience;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillInput {
        private Integer skillId;
        private String skillName;
        private Integer proficiencyLevel; // 1 to 5
        private Integer yearsExperience;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssessmentInput {
        private Integer skillId;
        private Double score;
        private Boolean verified;
    }
}
