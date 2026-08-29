package com.skillspherenexus.learningservice.dto;

import com.skillspherenexus.learningservice.enums.CourseLevel;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningPathUpdateRequestDTO {

    @NotBlank(message = "Learning path title is required")
    @Size(
            max = 200,
            message = "Learning path title cannot exceed 200 characters"
    )
    private String title;

    @Size(
            max = 5000,
            message = "Description cannot exceed 5000 characters"
    )
    private String description;

    @NotBlank(message = "Category is required")
    @Size(
            max = 100,
            message = "Category cannot exceed 100 characters"
    )
    private String category;

    @Size(
            max = 150,
            message = "Target role cannot exceed 150 characters"
    )
    private String targetRole;

    @NotNull(message = "Learning path level is required")
    private CourseLevel level;

    @Min(
            value = 0,
            message = "Estimated duration cannot be negative"
    )
    private Integer estimatedDurationHours;
}