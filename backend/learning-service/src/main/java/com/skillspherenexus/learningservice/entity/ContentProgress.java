package com.skillspherenexus.learningservice.entity;

import com.skillspherenexus.learningservice.enums.ContentProgressStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "content_progress",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_content_progress_enrollment_content",
                        columnNames = {
                                "enrollment_id",
                                "content_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_content_progress_enrollment",
                        columnList = "enrollment_id"
                ),
                @Index(
                        name = "idx_content_progress_content",
                        columnList = "content_id"
                ),
                @Index(
                        name = "idx_content_progress_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID progressId;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "enrollment_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_content_progress_enrollment"
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
                    name = "fk_content_progress_content"
            )
    )
    private CourseContent content;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private ContentProgressStatus status;

    @Column(
            name = "progress_percentage",
            nullable = false
    )
    private Integer progressPercentage;

    @Column(
            name = "last_position_seconds",
            nullable = false
    )
    private Long lastPositionSeconds;

    @Column(
            name = "time_spent_seconds",
            nullable = false
    )
    private Long timeSpentSeconds;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

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

        if (status == null) {
            status = ContentProgressStatus.NOT_STARTED;
        }

        if (progressPercentage == null) {
            progressPercentage = 0;
        }

        if (lastPositionSeconds == null) {
            lastPositionSeconds = 0L;
        }

        if (timeSpentSeconds == null) {
            timeSpentSeconds = 0L;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}