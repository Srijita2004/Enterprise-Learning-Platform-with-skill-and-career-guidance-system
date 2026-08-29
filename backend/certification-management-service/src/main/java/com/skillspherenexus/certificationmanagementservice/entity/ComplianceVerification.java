package com.skillspherenexus.certificationmanagementservice.entity;

import com.skillspherenexus.certificationmanagementservice.enums.ComplianceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "compliance_verifications", indexes = @Index(name = "idx_compliance_cert", columnList = "certification_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ComplianceVerification {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID complianceVerificationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "certification_id", nullable = false)
    private CertificationRecord certification;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ComplianceStatus result;

    @Column(name = "policy_reference", nullable = false, length = 180)
    private String policyReference;

    @Column(name = "evidence_reference", length = 280)
    private String evidenceReference;

    @Column(length = 800)
    private String notes;

    @Column(name = "verified_by_user_id", nullable = false, length = 80)
    private String verifiedByUserId;

    @Column(name = "verified_by_role", nullable = false, length = 30)
    private String verifiedByRole;

    @Column(name = "verified_at", nullable = false)
    private LocalDateTime verifiedAt;
}
