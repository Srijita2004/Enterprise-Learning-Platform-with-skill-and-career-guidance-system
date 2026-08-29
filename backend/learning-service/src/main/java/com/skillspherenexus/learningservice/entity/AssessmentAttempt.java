package com.skillspherenexus.learningservice.entity;

import com.skillspherenexus.learningservice.enums.AssessmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "assessment_attempts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_assessment_attempt",
                        columnNames = {
                                "enrollment_id",
                                "content_id",
                                "attempt_number"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_assessment_attempt_enrollment",
                        columnList = "enrollment_id"
                ),
                @Index(
                        name = "idx_assessment_attempt_content",
                        columnList = "content_id"
                ),
                @Index(
                        name = "idx_assessment_attempt_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID attemptId;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "enrollment_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_assessment_attempt_enrollment"
            )
    )
    private Enrollment enrollment;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "content_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_assessment_attempt_content"
            )
    )
    private CourseContent content;

    @Column(
            name = "attempt_number",
            nullable = false
    )
    private Integer attemptNumber;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private AssessmentStatus status;

    @Column(
            precision = 10,
            scale = 2
    )
    private BigDecimal score;

    @Column(
            name = "maximum_score",
            precision = 10,
            scale = 2
    )
    private BigDecimal maximumScore;

    @Column(
            name = "score_percentage",
            precision = 5,
            scale = 2
    )
    private BigDecimal scorePercentage;

    @Column(name = "graded_by_user_id")
    private UUID gradedByUserId;

    @Column(length = 1000)
    private String feedback;

    @Column(
            name = "started_at",
            nullable = false
    )
    private LocalDateTime startedAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "graded_at")
    private LocalDateTime gradedAt;

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

        if (attemptNumber == null) {
            attemptNumber = 1;
        }

        if (status == null) {
            status = AssessmentStatus.IN_PROGRESS;
        }

        if (startedAt == null) {
            startedAt = currentTime;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
