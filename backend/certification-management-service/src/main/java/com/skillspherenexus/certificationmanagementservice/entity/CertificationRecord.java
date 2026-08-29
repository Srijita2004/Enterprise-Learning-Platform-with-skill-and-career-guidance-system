package com.skillspherenexus.certificationmanagementservice.entity;

import com.skillspherenexus.certificationmanagementservice.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "certification_records", uniqueConstraints = {
        @UniqueConstraint(name = "uk_cert_credential_number", columnNames = "credential_number"),
        @UniqueConstraint(name = "uk_cert_legacy_id", columnNames = "legacy_certificate_id")
}, indexes = {
        @Index(name = "idx_cert_employee", columnList = "employee_id"),
        @Index(name = "idx_cert_expiry", columnList = "expiry_date"),
        @Index(name = "idx_cert_status", columnList = "status"),
        @Index(name = "idx_cert_renewal_status", columnList = "renewal_status")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CertificationRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID certificationId;

    @Column(name = "employee_id", nullable = false)
    private Integer employeeId;

    @Column(name = "employee_name", nullable = false, length = 150)
    private String employeeName;

    @Column(name = "certification_name", nullable = false, length = 180)
    private String certificationName;

    @Column(name = "issuing_organization", nullable = false, length = 180)
    private String issuingOrganization;

    @Column(name = "credential_number", length = 180)
    private String credentialNumber;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CertificationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "renewal_status", nullable = false, length = 30)
    private RenewalStatus renewalStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 30)
    private VerificationStatus verificationStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "compliance_status", nullable = false, length = 30)
    private ComplianceStatus complianceStatus;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "warning_window_days", nullable = false)
    private Integer warningWindowDays;

    @Column(name = "renewal_due_date")
    private LocalDate renewalDueDate;

    @Column(name = "legacy_certificate_id")
    private Integer legacyCertificateId;

    @Column(name = "source_system", nullable = false, length = 40)
    private String sourceSystem;

    @Column(name = "last_evaluated_at")
    private LocalDateTime lastEvaluatedAt;

    @Column(name = "created_by_user_id", length = 80)
    private String createdByUserId;

    @Column(name = "created_by_role", length = 30)
    private String createdByRole;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (active == null) active = true;
        if (sourceSystem == null || sourceSystem.isBlank()) sourceSystem = "M3";
    }

    @PreUpdate
    void preUpdate() { updatedAt = LocalDateTime.now(); }
}
