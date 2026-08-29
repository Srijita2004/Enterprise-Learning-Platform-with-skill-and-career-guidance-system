package com.skillspherenexus.learningservice.entity;

import com.skillspherenexus.learningservice.enums.LearningPathAssignmentSource;
import com.skillspherenexus.learningservice.enums.LearningPathAssignmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "learning_path_assignments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_learning_path_assignment",
                        columnNames = {
                                "path_id",
                                "learner_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_path_assignment_path",
                        columnList = "path_id"
                ),
                @Index(
                        name = "idx_path_assignment_learner",
                        columnList = "learner_id"
                ),
                @Index(
                        name = "idx_path_assignment_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningPathAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "assignment_id")
    private UUID assignmentId;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "path_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_path_assignment_path"
            )
    )
    private LearningPath learningPath;

    @Column(
            name = "learner_id",
            nullable = false
    )
    private UUID learnerId;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private LearningPathAssignmentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "assignment_source",
            nullable = false,
            length = 40
    )
    private LearningPathAssignmentSource assignmentSource;

    @Column(name = "assigned_by_user_id")
    private UUID assignedByUserId;

    @Column(
            name = "progress_percentage",
            nullable = false,
            precision = 5,
            scale = 2
    )
    private BigDecimal progressPercentage;

    @Column(name = "current_course_order")
    private Integer currentCourseOrder;

    @Column(
            name = "assigned_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime assignedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "due_at")
    private LocalDateTime dueAt;

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

        if (assignedAt == null) {
            assignedAt = currentTime;
        }

        if (status == null) {
            status =
                    LearningPathAssignmentStatus.ASSIGNED;
        }

        if (assignmentSource == null) {
            assignmentSource =
                    LearningPathAssignmentSource.SELF_ASSIGNED;
        }

        if (progressPercentage == null) {
            progressPercentage =
                    BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
