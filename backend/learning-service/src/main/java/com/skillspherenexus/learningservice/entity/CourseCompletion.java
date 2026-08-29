package com.skillspherenexus.learningservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "course_completions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_course_completion_enrollment",
                        columnNames = "enrollment_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_course_completion_learner",
                        columnList = "learner_id"
                ),
                @Index(
                        name = "idx_course_completion_course",
                        columnList = "course_id"
                ),
                @Index(
                        name = "idx_course_completion_completed_at",
                        columnList = "completed_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID completionId;

    @OneToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "enrollment_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(
                    name = "fk_course_completion_enrollment"
            )
    )
    private Enrollment enrollment;

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
            name = "overall_progress_percentage",
            nullable = false,
            precision = 5,
            scale = 2
    )
    private BigDecimal overallProgressPercentage;

    @Column(
            name = "mandatory_progress_percentage",
            nullable = false,
            precision = 5,
            scale = 2
    )
    private BigDecimal mandatoryProgressPercentage;

    @Column(
            name = "total_published_contents",
            nullable = false
    )
    private Long totalPublishedContents;

    @Column(
            name = "completed_contents",
            nullable = false
    )
    private Long completedContents;

    @Column(
            name = "total_mandatory_contents",
            nullable = false
    )
    private Long totalMandatoryContents;

    @Column(
            name = "completed_mandatory_contents",
            nullable = false
    )
    private Long completedMandatoryContents;

    @Column(
            name = "mandatory_assessments_passed",
            nullable = false
    )
    private Boolean mandatoryAssessmentsPassed;

    @Column(
            name = "certificate_eligible",
            nullable = false
    )
    private Boolean certificateEligible;

    @Column(
            name = "total_time_spent_seconds",
            nullable = false
    )
    private Long totalTimeSpentSeconds;

    @Column(
            name = "completed_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime completedAt;

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

        if (completedAt == null) {
            completedAt = currentTime;
        }

        if (mandatoryAssessmentsPassed == null) {
            mandatoryAssessmentsPassed = false;
        }

        if (certificateEligible == null) {
            certificateEligible = false;
        }

        if (totalTimeSpentSeconds == null) {
            totalTimeSpentSeconds = 0L;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}