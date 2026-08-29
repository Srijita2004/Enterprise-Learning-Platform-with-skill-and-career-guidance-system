package com.skillspherenexus.certificationmanagementservice.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record CertificationUpdateRequest(
        @NotBlank @Size(max = 180) String certificationName,
        @NotBlank @Size(max = 180) String issuingOrganization,
        @Size(max = 180) String credentialNumber,
        @NotNull @PastOrPresent LocalDate issueDate,
        LocalDate expiryDate,
        @Min(1) @Max(365) Integer warningWindowDays
) {}
