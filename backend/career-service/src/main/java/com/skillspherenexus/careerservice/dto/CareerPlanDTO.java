package com.skillspherenexus.careerservice.dto;

import com.skillspherenexus.careerservice.entity.CareerPlanStatus;
import lombok.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CareerPlanDTO {
    private Long id;
    
    @NotNull(message = "Employee ID is required")
    private Long employeeId;
    
    @NotBlank(message = "Employee name is required")
    private String employeeName;
    
    @NotBlank(message = "Current role is required")
    private String currentRole;
    
    @NotBlank(message = "Target role is required")
    private String targetRole;
    
    @Min(value = 0, message = "Progress must be between 0 and 100")
    @Max(value = 100, message = "Progress must be between 0 and 100")
    private Integer progressPercentage;
    
    private String mentorId;
    private String mentorName;
    
    private CareerPlanStatus status;
    
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;
    
    private LocalDateTime startDate;
    private LocalDateTime targetCompletionDate;
    private LocalDateTime completionDate;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private List<PromotionCriteriaDTO> promotionCriteria;
    private List<SkillGapDTO> skillGaps;
}
