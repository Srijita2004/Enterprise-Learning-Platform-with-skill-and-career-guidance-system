package com.skillspherenexus.certificationmanagementservice.dto;

import com.skillspherenexus.certificationmanagementservice.enums.ComplianceStatus;
import jakarta.validation.constraints.*;

public record ComplianceVerifyRequest(
        @NotNull ComplianceStatus result,
        @NotBlank @Size(max = 180) String policyReference,
        @Size(max = 280) String evidenceReference,
        @Size(max = 800) String notes
) {}
