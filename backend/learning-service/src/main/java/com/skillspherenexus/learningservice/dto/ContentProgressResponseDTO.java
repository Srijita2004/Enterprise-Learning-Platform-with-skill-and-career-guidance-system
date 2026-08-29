package com.skillspherenexus.learningservice.dto;

import com.skillspherenexus.learningservice.enums.ContentProgressStatus;
import com.skillspherenexus.learningservice.enums.ContentType;
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
public class ContentProgressResponseDTO {

    private UUID progressId;

    private UUID enrollmentId;

    private UUID learnerId;

    private UUID courseId;

    private String courseCode;

    private String courseTitle;

    private UUID moduleId;

    private String moduleTitle;

    private Integer moduleOrder;

    private UUID contentId;

    private String contentTitle;

    private ContentType contentType;

    private Integer contentOrder;

    private Integer durationMinutes;

    private Boolean mandatory;

    private ContentProgressStatus status;

    private Integer progressPercentage;

    private Long lastPositionSeconds;

    private Long timeSpentSeconds;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private LocalDateTime lastAccessedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}