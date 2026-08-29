package com.skillspherenexus.learningservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentGradeRequestDTO {

    @NotNull(message = "Score is required")
    @DecimalMin(
            value = "0.00",
            inclusive = true,
            message = "Score cannot be negative"
    )
    private BigDecimal score;

    @NotNull(message = "Maximum score is required")
    @DecimalMin(
            value = "0.01",
            inclusive = true,
            message = "Maximum score must be greater than zero"
    )
    private BigDecimal maximumScore;

    private UUID gradedByUserId;

    @Size(
            max = 1000,
            message = "Feedback cannot exceed 1000 characters"
    )
    private String feedback;
}