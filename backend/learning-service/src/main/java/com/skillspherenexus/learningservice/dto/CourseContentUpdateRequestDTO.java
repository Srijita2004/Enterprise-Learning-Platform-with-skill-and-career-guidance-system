package com.skillspherenexus.learningservice.dto;

import com.skillspherenexus.learningservice.enums.ContentType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseContentUpdateRequestDTO {

    @NotBlank(message = "Content title is required")
    @Size(
            max = 150,
            message = "Content title cannot exceed 150 characters"
    )
    private String title;

    @Size(
            max = 5000,
            message = "Content description cannot exceed 5000 characters"
    )
    private String description;

    @NotNull(message = "Content type is required")
    private ContentType contentType;

    @Size(
            max = 1000,
            message = "Content URL cannot exceed 1000 characters"
    )
    private String contentUrl;

    @Size(
            max = 20000,
            message = "Text content cannot exceed 20000 characters"
    )
    private String textContent;

    @Min(
            value = 1,
            message = "Duration must be at least 1 minute"
    )
    private Integer durationMinutes;

    @NotNull(message = "Content order is required")
    @Min(
            value = 1,
            message = "Content order must be at least 1"
    )
    private Integer contentOrder;

    private Boolean mandatory;

    private Boolean previewAvailable;

    private LocalDateTime availableFrom;

    private LocalDateTime availableUntil;
}