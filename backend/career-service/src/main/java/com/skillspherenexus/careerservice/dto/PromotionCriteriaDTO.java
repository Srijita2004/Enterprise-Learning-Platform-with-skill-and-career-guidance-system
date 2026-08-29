package com.skillspherenexus.careerservice.dto;

import com.skillspherenexus.careerservice.entity.PromotionCriteriaType;
import lombok.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionCriteriaDTO {
    private Long id;
    
    private Long careerPlanId;
    
    @NotBlank(message = "Criteria name is required")
    private String name;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    @NotNull(message = "Criteria type is required")
    private PromotionCriteriaType type;
    
    @NotNull(message = "Status (met/not met) is required")
    private Boolean isMet;
    
    private LocalDateTime metDate;
    
    @Positive(message = "Sequence order must be positive")
    private Integer sequenceOrder;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
