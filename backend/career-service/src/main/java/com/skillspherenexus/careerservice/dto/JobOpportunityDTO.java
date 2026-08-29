package com.skillspherenexus.careerservice.dto;

import com.skillspherenexus.careerservice.entity.JobStatus;
import lombok.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobOpportunityDTO {
    private Long id;
    
    @NotBlank(message = "Job title is required")
    private String jobTitle;
    
    @NotBlank(message = "Department is required")
    private String department;
    
    @NotBlank(message = "Location is required")
    private String location;
    
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;
    
    @Size(max = 1000, message = "Required skills cannot exceed 1000 characters")
    private String requiredSkills;
    
    @PositiveOrZero(message = "Salary must be >= 0")
    @NotNull(message = "Salary is required")
    private Double salary;
    
    @NotNull(message = "Job status is required")
    private JobStatus status;
    
    @PositiveOrZero(message = "Required experience must be >= 0")
    @NotNull(message = "Required experience is required")
    private Integer requiredExperienceYears;
    
    private String managerId;
    private String managerName;
    
    private LocalDateTime postedDate;
    private LocalDateTime closedDate;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
