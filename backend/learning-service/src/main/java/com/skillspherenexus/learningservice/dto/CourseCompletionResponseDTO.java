package com.skillspherenexus.learningservice.dto;

import com.skillspherenexus.learningservice.enums.EnrollmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseCompletionResponseDTO {

    private UUID completionId;

    private UUID enrollmentId;

    private UUID learnerId;

    private UUID courseId;

    private String courseCode;

    private String courseTitle;

    private EnrollmentStatus enrollmentStatus;

    private BigDecimal overallProgressPercentage;

    private BigDecimal mandatoryProgressPercentage;

    private Long totalPublishedContents;

    private Long completedContents;

    private Long totalMandatoryContents;

    private Long completedMandatoryContents;

    private Boolean mandatoryAssessmentsPassed;

    private Boolean certificateEnabled;

    private Boolean certificateEligible;

    private Long totalTimeSpentSeconds;

    private LocalDateTime enrolledAt;

    private LocalDateTime completedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}