package com.skillspherenexus.learningservice.dto;

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
public class CourseContentResponseDTO {

    private UUID contentId;

    private UUID moduleId;

    private String moduleTitle;

    private UUID courseId;

    private String courseCode;

    private String courseTitle;

    private String title;

    private String description;

    private ContentType contentType;

    private String contentUrl;

    private String textContent;

    private Integer durationMinutes;

    private Integer contentOrder;

    private Boolean mandatory;

    private Boolean previewAvailable;

    private Boolean published;

    private LocalDateTime availableFrom;

    private LocalDateTime availableUntil;

    private Boolean currentlyAvailable;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}