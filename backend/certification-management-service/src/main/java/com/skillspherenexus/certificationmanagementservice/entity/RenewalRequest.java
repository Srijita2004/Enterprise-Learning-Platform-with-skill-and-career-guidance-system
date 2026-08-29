package com.skillspherenexus.certificationmanagementservice.entity;

import com.skillspherenexus.certificationmanagementservice.enums.RenewalRequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "renewal_requests", indexes = {
        @Index(name = "idx_renewal_cert", columnList = "certification_id"),
        @Index(name = "idx_renewal_status", columnList = "status")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RenewalRequest {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID renewalRequestId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "certification_id", nullable = false)
    private CertificationRecord certification;

    @Column(name = "current_expiry_date")
    private LocalDate currentExpiryDate;

    @Column(name = "proposed_expiry_date", nullable = false)
    private LocalDate proposedExpiryDate;

    @Column(nullable = false, length = 600)
    private String justification;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RenewalRequestStatus status;

    @Column(name = "requested_by_user_id", nullable = false, length = 80)
    private String requestedByUserId;

    @Column(name = "requested_by_role", nullable = false, length = 30)
    private String requestedByRole;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "decision_by_user_id", length = 80)
    private String decisionByUserId;

    @Column(name = "decision_by_role", length = 30)
    private String decisionByRole;

    @Column(name = "decision_note", length = 600)
    private String decisionNote;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    @Column(name = "on_time")
    private Boolean onTime;
}
