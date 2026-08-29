package com.skillspherenexus.learningservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentProgressUpdateRequestDTO {

    @NotNull(message = "Progress percentage is required")
    @Min(
            value = 0,
            message = "Progress percentage cannot be less than 0"
    )
    @Max(
            value = 100,
            message = "Progress percentage cannot exceed 100"
    )
    private Integer progressPercentage;

    @PositiveOrZero(
            message = "Last position cannot be negative"
    )
    private Long lastPositionSeconds;

    @PositiveOrZero(
            message = "Additional time spent cannot be negative"
    )
    private Long additionalTimeSpentSeconds;
}