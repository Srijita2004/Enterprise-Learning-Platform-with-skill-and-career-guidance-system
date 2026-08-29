package com.skillspherenexus.careerservice.dto;

import lombok.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillGapDTO {
    private Long id;
    
    private Long careerPlanId;
    
    @NotBlank(message = "Skill name is required")
    private String skillName;
    
    @Min(value = 0, message = "Current level must be >= 0")
    @Max(value = 10, message = "Current level must be <= 10")
    @NotNull(message = "Current level is required")
    private Integer currentLevel;
    
    @Min(value = 0, message = "Required level must be >= 0")
    @Max(value = 10, message = "Required level must be <= 10")
    @NotNull(message = "Required level is required")
    private Integer requiredLevel;
    
    private Integer gapLevel;
    
    @Size(max = 2000, message = "Training plan cannot exceed 2000 characters")
    private String trainingPlan;
    
    private LocalDateTime identifiedDate;
    private LocalDateTime filledDate;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
