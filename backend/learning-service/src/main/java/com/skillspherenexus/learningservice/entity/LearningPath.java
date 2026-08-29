package com.skillspherenexus.learningservice.entity;

import com.skillspherenexus.learningservice.enums.CourseLevel;
import com.skillspherenexus.learningservice.enums.LearningPathStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "learning_paths",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_learning_path_code",
                        columnNames = "path_code"
                )
        },
        indexes = {
                @Index(
                        name = "idx_learning_path_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_learning_path_category",
                        columnList = "category"
                ),
                @Index(
                        name = "idx_learning_path_level",
                        columnList = "level"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningPath {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "path_id")
    private UUID pathId;

    @Column(
            name = "path_code",
            nullable = false,
            unique = true,
            length = 50
    )
    private String pathCode;

    @Column(
            nullable = false,
            length = 200
    )
    private String title;

    @Column(
            columnDefinition = "TEXT"
    )
    private String description;

    @Column(
            nullable = false,
            length = 100
    )
    private String category;

    @Column(
            name = "target_role",
            length = 150
    )
    private String targetRole;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private CourseLevel level;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private LearningPathStatus status;

    @Column(
            name = "estimated_duration_hours"
    )
    private Integer estimatedDurationHours;

    @Column(
            name = "created_by_user_id"
    )
    private UUID createdByUserId;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

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
            status = LearningPathStatus.DRAFT;
        }

        if (level == null) {
            level = CourseLevel.BEGINNER;
        }

        if (estimatedDurationHours == null) {
            estimatedDurationHours = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}