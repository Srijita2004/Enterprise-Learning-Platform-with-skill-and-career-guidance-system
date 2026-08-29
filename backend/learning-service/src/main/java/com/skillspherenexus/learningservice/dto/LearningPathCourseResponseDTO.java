package com.skillspherenexus.learningservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningPathCourseResponseDTO {

    private UUID pathCourseId;

    private UUID pathId;

    private UUID courseId;

    private String courseCode;

    private String courseTitle;

    private Integer courseOrder;

    private Boolean requiredForCompletion;

    private Boolean unlockAfterPrevious;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}