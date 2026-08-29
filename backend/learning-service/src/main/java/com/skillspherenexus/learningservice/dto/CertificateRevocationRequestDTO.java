package com.skillspherenexus.learningservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateRevocationRequestDTO {

    @NotNull(message = "Revoked-by user ID is required")
    private UUID revokedByUserId;

    @NotBlank(message = "Revocation reason is required")
    @Size(
            max = 500,
            message = "Revocation reason cannot exceed 500 characters"
    )
    private String revocationReason;
}