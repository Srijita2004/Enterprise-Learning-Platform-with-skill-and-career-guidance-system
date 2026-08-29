package com.skillspherenexus.learningservice.dto;

import com.skillspherenexus.learningservice.enums.AssessmentStatus;
import com.skillspherenexus.learningservice.enums.ContentType;
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
public class AssessmentAttemptResponseDTO {

    private UUID attemptId;

    private UUID enrollmentId;

    private UUID learnerId;

    private UUID courseId;

    private String courseCode;

    private String courseTitle;

    private Integer passingScore;

    private UUID moduleId;

    private String moduleTitle;

    private Integer moduleOrder;

    private UUID contentId;

    private String contentTitle;

    private ContentType contentType;

    private Integer contentOrder;

    private Integer attemptNumber;

    private AssessmentStatus status;

    private BigDecimal score;

    private BigDecimal maximumScore;

    private BigDecimal scorePercentage;

    private UUID gradedByUserId;

    private String feedback;

    private LocalDateTime startedAt;

    private LocalDateTime submittedAt;

    private LocalDateTime gradedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}