package com.skillspherenexus.careerservice.dto;

import lombok.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CareerRoadmapDTO {
    private Long id;
    
    @NotBlank(message = "Roadmap name is required")
    private String name;
    
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;
    
    @NotBlank(message = "Source role is required")
    private String sourceRole;
    
    @NotBlank(message = "Target role is required")
    private String targetRole;
    
    @Positive(message = "Estimated duration must be positive")
    @NotNull(message = "Estimated duration is required")
    private Integer estimatedDurationMonths;
    
    @Size(max = 2000, message = "Required skills cannot exceed 2000 characters")
    private String requiredSkills;
    
    @Size(max = 2000, message = "Suggested courses cannot exceed 2000 characters")
    private String suggestedCourses;
    
    @NotNull(message = "Active status is required")
    private Boolean isActive;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
