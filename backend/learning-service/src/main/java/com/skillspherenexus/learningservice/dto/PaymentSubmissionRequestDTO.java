package com.skillspherenexus.learningservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSubmissionRequestDTO {

    @NotBlank(message = "Payment reference is required")
    @Size(max = 120, message = "Payment reference cannot exceed 120 characters")
    private String paymentReference;
}
