package com.skillspherenexus.certificationmanagementservice.dto;

import com.skillspherenexus.certificationmanagementservice.enums.*;
import java.time.*;
import java.util.UUID;

public record CertificationResponse(
        UUID certificationId, Integer employeeId, String employeeName, String certificationName,
        String issuingOrganization, String credentialNumber, LocalDate issueDate, LocalDate expiryDate,
        Long daysRemaining, CertificationStatus status, RenewalStatus renewalStatus,
        VerificationStatus verificationStatus, ComplianceStatus complianceStatus,
        boolean active, int warningWindowDays, LocalDate renewalDueDate,
        Integer legacyCertificateId, String sourceSystem, LocalDateTime lastEvaluatedAt,
        LocalDateTime createdAt, LocalDateTime updatedAt
) {}
