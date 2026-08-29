package com.skillspherenexus.learningservice.dto;

import com.skillspherenexus.learningservice.enums.EnrollmentSource;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class EnrollmentCreateRequestDTO {

    @NotNull(message = "Learner ID is required")
    private UUID learnerId;

    @NotNull(message = "Enrollment source is required")
    private EnrollmentSource enrollmentSource;

    private UUID assignedByUserId;

    @Size(max = 120, message = "Payment reference cannot exceed 120 characters")
    private String paymentReference;

    @FutureOrPresent(
            message = "Access expiry time cannot be in the past"
    )
    private LocalDateTime accessExpiresAt;
}