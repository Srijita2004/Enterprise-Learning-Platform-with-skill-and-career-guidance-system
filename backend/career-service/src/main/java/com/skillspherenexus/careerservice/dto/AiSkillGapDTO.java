package com.skillspherenexus.careerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiSkillGapDTO {
    private String skillName;
    private Integer currentLevel;  // 0 to 5
    private Integer requiredLevel; // 1 to 5
    private Integer gapLevel;      // required - current
    private String priority;       // HIGH, MEDIUM, LOW
    private String recommendedAction;
    private String targetCourse;
}
