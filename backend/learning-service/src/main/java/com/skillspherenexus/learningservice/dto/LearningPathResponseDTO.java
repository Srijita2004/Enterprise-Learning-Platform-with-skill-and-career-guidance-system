package com.skillspherenexus.learningservice.dto;

import com.skillspherenexus.learningservice.enums.CourseLevel;
import com.skillspherenexus.learningservice.enums.LearningPathStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningPathResponseDTO {

    private UUID pathId;

    private String pathCode;

    private String title;

    private String description;

    private String category;

    private String targetRole;

    private CourseLevel level;

    private LearningPathStatus status;

    private Integer estimatedDurationHours;

    private Long totalCourses;

    private Long requiredCourses;

    private Long totalAssignments;

    private UUID createdByUserId;

    private LocalDateTime publishedAt;

    private LocalDateTime archivedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}