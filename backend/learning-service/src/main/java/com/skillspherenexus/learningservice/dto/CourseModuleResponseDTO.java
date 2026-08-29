package com.skillspherenexus.learningservice.dto;

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
public class CourseModuleResponseDTO {

    private UUID moduleId;

    private UUID courseId;

    private String courseCode;

    private String courseTitle;

    private String title;

    private String description;

    private Integer moduleOrder;

    private Boolean published;

    private Long contentCount;

    private Long publishedContentCount;

    private Integer totalDurationMinutes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}