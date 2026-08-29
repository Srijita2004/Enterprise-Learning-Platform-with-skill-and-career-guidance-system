package com.skillspherenexus.learningservice.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearnerDashboardResponseDTO {

    private UUID learnerId;

    private Long totalEnrollments;

    private Long activeEnrollments;

    private Long completedCourses;

    private Long pendingEnrollments;

    private BigDecimal averageCourseProgress;

    private Long totalAssessmentAttempts;

    private Long passedAssessments;

    private Long failedAssessments;

    private Long certificatesEarned;

    private Long assignedLearningPaths;

    private Long inProgressLearningPaths;

    private Long completedLearningPaths;

    private LocalDateTime generatedAt;
}