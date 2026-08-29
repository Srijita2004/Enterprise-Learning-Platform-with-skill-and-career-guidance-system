package com.skillspherenexus.learningservice.dto;

import jakarta.validation.constraints.NotBlank;
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
public class CertificateIssueRequestDTO {

    @NotBlank(message = "Recipient name is required")
    @Size(
            max = 150,
            message = "Recipient name cannot exceed 150 characters"
    )
    private String recipientName;

    private UUID issuedByUserId;

    @Size(
            max = 1000,
            message = "Certificate file URL cannot exceed 1000 characters"
    )
    private String fileUrl;
}