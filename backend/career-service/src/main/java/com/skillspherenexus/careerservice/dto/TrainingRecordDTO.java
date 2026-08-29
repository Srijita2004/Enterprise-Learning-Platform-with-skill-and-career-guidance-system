package com.skillspherenexus.careerservice.dto;

import com.skillspherenexus.careerservice.entity.TrainingStatus;
import lombok.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingRecordDTO {
    private Long id;
    
    @NotNull(message = "Employee ID is required")
    private Long employeeId;
    
    @NotBlank(message = "Course name is required")
    private String courseName;
    
    @NotBlank(message = "Course ID is required")
    private String courseId;
    
    @Min(value = 0, message = "Score must be >= 0")
    @Max(value = 100, message = "Score must be <= 100")
    private Integer score;
    
    @Min(value = 0, message = "Completion percentage must be >= 0")
    @Max(value = 100, message = "Completion percentage must be <= 100")
    private Integer completionPercentage;
    
    @NotNull(message = "Training status is required")
    private TrainingStatus status;
    
    private LocalDateTime enrollmentDate;
    private LocalDateTime completionDate;
    
    @Size(max = 1000, message = "Feedback cannot exceed 1000 characters")
    private String feedback;
    
    private Double skillImprovement;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
