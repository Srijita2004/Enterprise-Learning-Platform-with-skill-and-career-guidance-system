package com.skillspherenexus.certificationmanagementservice.dto;

import com.skillspherenexus.certificationmanagementservice.enums.ComplianceStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record ComplianceVerificationResponse(UUID complianceVerificationId, UUID certificationId, String certificationName,
        Integer employeeId, String employeeName, ComplianceStatus result, String policyReference,
        String evidenceReference, String notes, String verifiedByUserId, String verifiedByRole, LocalDateTime verifiedAt) {}
