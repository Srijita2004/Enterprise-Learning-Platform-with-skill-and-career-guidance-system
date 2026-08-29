package com.skillspherenexus.learningservice.entity;

import com.skillspherenexus.learningservice.enums.CertificateStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "certificates",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_certificate_completion",
                        columnNames = "completion_id"
                ),
                @UniqueConstraint(
                        name = "uk_certificate_number",
                        columnNames = "certificate_number"
                ),
                @UniqueConstraint(
                        name = "uk_certificate_verification_code",
                        columnNames = "verification_code"
                )
        },
        indexes = {
                @Index(
                        name = "idx_certificate_learner",
                        columnList = "learner_id"
                ),
                @Index(
                        name = "idx_certificate_course",
                        columnList = "course_id"
                ),
                @Index(
                        name = "idx_certificate_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_certificate_verification",
                        columnList = "verification_code"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID certificateId;

    @OneToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "completion_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(
                    name = "fk_certificate_completion"
            )
    )
    private CourseCompletion courseCompletion;

    @Column(
            name = "learner_id",
            nullable = false,
            updatable = false
    )
    private UUID learnerId;

    @Column(
            name = "course_id",
            nullable = false,
            updatable = false
    )
    private UUID courseId;

    @Column(
            name = "course_code",
            nullable = false,
            updatable = false,
            length = 50
    )
    private String courseCode;

    @Column(
            name = "course_title",
            nullable = false,
            updatable = false,
            length = 200
    )
    private String courseTitle;

    @Column(
            name = "recipient_name",
            nullable = false,
            updatable = false,
            length = 150
    )
    private String recipientName;

    @Column(
            name = "certificate_number",
            nullable = false,
            unique = true,
            updatable = false,
            length = 50
    )
    private String certificateNumber;

    @Column(
            name = "verification_code",
            nullable = false,
            unique = true,
            updatable = false
    )
    private UUID verificationCode;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private CertificateStatus status;

    @Column(name = "issued_by_user_id")
    private UUID issuedByUserId;

    @Column(name = "revoked_by_user_id")
    private UUID revokedByUserId;

    @Column(
            name = "file_url",
            length = 1000
    )
    private String fileUrl;

    @Column(
            name = "course_completed_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime courseCompletedAt;

    @Column(
            name = "issued_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime issuedAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(
            name = "revocation_reason",
            length = 500
    )
    private String revocationReason;

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

        if (issuedAt == null) {
            issuedAt = currentTime;
        }

        if (verificationCode == null) {
            verificationCode = UUID.randomUUID();
        }

        if (status == null) {
            status = CertificateStatus.ISSUED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}