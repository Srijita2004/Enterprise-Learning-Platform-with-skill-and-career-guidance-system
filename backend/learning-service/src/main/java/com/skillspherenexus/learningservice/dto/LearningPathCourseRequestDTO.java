package com.skillspherenexus.learningservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningPathCourseRequestDTO {

    @NotNull(message = "Course ID is required")
    private UUID courseId;

    @NotNull(message = "Course order is required")
    @Min(
            value = 1,
            message = "Course order must be at least 1"
    )
    private Integer courseOrder;

    private Boolean requiredForCompletion;

    private Boolean unlockAfterPrevious;
}