package com.skillspherenexus.certificationmanagementservice.dto;

import com.skillspherenexus.certificationmanagementservice.enums.RenewalRequestStatus;
import java.time.*;
import java.util.UUID;

public record RenewalRequestResponse(
        UUID renewalRequestId, UUID certificationId, String certificationName, Integer employeeId, String employeeName,
        LocalDate currentExpiryDate, LocalDate proposedExpiryDate, String justification, RenewalRequestStatus status,
        String requestedByUserId, String requestedByRole, LocalDateTime requestedAt,
        String decisionByUserId, String decisionByRole, String decisionNote, LocalDateTime decidedAt, Boolean onTime
) {}
