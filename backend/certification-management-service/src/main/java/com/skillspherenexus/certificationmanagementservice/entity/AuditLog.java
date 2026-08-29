package com.skillspherenexus.certificationmanagementservice.entity;

import com.skillspherenexus.certificationmanagementservice.enums.AuditAction;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Immutable
@Table(name = "certification_audit_log", indexes = {
        @Index(name = "idx_audit_cert", columnList = "certification_id"),
        @Index(name = "idx_audit_created", columnList = "created_at")
})
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED) @AllArgsConstructor @Builder
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sequenceNumber;

    @Column(name = "audit_id", nullable = false, unique = true, updatable = false)
    private UUID auditId;

    @Column(name = "certification_id", updatable = false)
    private UUID certificationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50, updatable = false)
    private AuditAction action;

    @Column(name = "actor_user_id", length = 80, updatable = false)
    private String actorUserId;

    @Column(name = "actor_role", length = 30, updatable = false)
    private String actorRole;

    @Column(nullable = false, length = 1000, updatable = false)
    private String details;

    @Column(name = "before_state", columnDefinition = "TEXT", updatable = false)
    private String beforeState;

    @Column(name = "after_state", columnDefinition = "TEXT", updatable = false)
    private String afterState;

    @Column(name = "previous_hash", length = 64, updatable = false)
    private String previousHash;

    @Column(name = "event_hash", nullable = false, length = 64, updatable = false)
    private String eventHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
