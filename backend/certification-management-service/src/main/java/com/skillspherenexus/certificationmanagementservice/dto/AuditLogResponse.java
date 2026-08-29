package com.skillspherenexus.certificationmanagementservice.dto;
import com.skillspherenexus.certificationmanagementservice.enums.AuditAction;
import java.time.LocalDateTime;
import java.util.UUID;
public record AuditLogResponse(Long sequenceNumber, UUID auditId, UUID certificationId, AuditAction action,
        String actorUserId, String actorRole, String details, String previousHash, String eventHash, LocalDateTime createdAt) {}
