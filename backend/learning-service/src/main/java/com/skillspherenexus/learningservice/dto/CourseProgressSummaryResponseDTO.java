package com.skillspherenexus.learningservice.dto;

import com.skillspherenexus.learningservice.enums.EnrollmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseProgressSummaryResponseDTO {

    private UUID enrollmentId;

    private UUID learnerId;

    private UUID courseId;

    private String courseCode;

    private String courseTitle;

    private EnrollmentStatus enrollmentStatus;

    private Long totalPublishedContents;

    private Long completedContents;

    private Long inProgressContents;

    private Long notStartedContents;

    private Long totalMandatoryContents;

    private Long completedMandatoryContents;

    private Double overallProgressPercentage;

    private Double mandatoryProgressPercentage;

    private Long totalTimeSpentSeconds;

    private LocalDateTime lastAccessedAt;

    private Boolean allMandatoryContentsCompleted;
}