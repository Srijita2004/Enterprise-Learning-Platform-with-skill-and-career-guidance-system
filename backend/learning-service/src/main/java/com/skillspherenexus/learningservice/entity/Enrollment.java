package com.skillspherenexus.learningservice.entity;

import com.skillspherenexus.learningservice.enums.EnrollmentSource;
import com.skillspherenexus.learningservice.enums.EnrollmentStatus;
import com.skillspherenexus.learningservice.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "enrollments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_enrollment_learner_course",
                        columnNames = {
                                "learner_id",
                                "course_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_enrollment_learner",
                        columnList = "learner_id"
                ),
                @Index(
                        name = "idx_enrollment_course",
                        columnList = "course_id"
                ),
                @Index(
                        name = "idx_enrollment_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID enrollmentId;

    @Column(
            name = "learner_id",
            nullable = false
    )
    private UUID learnerId;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "course_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_enrollment_course"
            )
    )
    private Course course;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private EnrollmentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "payment_status",
            nullable = false,
            length = 30
    )
    private PaymentStatus paymentStatus;

    @Column(name = "payment_reference", length = 120)
    private String paymentReference;

    @Column(name = "payment_submitted_at")
    private LocalDateTime paymentSubmittedAt;

    @Column(name = "payment_verified_at")
    private LocalDateTime paymentVerifiedAt;

    @Column(name = "payment_verified_by_user_id")
    private UUID paymentVerifiedByUserId;

    @Column(name = "payment_rejection_reason", length = 500)
    private String paymentRejectionReason;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "enrollment_source",
            nullable = false,
            length = 40
    )
    private EnrollmentSource enrollmentSource;

    @Column(
            name = "assigned_by_user_id"
    )
    private UUID assignedByUserId;

    @Column(
            name = "price_at_enrollment",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal priceAtEnrollment;

    @Column(
            name = "currency_code",
            nullable = false,
            length = 3
    )
    private String currencyCode;

    @Column(
            name = "enrolled_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime enrolledAt;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "access_expires_at")
    private LocalDateTime accessExpiresAt;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @Column(
            name = "cancellation_reason",
            length = 500
    )
    private String cancellationReason;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        LocalDateTime currentTime = LocalDateTime.now();

        createdAt = currentTime;
        updatedAt = currentTime;

        if (enrolledAt == null) {
            enrolledAt = currentTime;
        }

        if (status == null) {
            status = EnrollmentStatus.PENDING;
        }

        if (paymentStatus == null) {
            paymentStatus = PaymentStatus.PENDING;
        }

        if (enrollmentSource == null) {
            enrollmentSource =
                    EnrollmentSource.SELF_ENROLLED;
        }

        if (priceAtEnrollment == null) {
            priceAtEnrollment = BigDecimal.ZERO;
        }

        if (currencyCode == null
                || currencyCode.isBlank()) {
            currencyCode = "INR";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}