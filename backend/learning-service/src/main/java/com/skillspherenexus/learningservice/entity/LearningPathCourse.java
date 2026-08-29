package com.skillspherenexus.learningservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "learning_path_courses",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_learning_path_course",
                        columnNames = {
                                "path_id",
                                "course_id"
                        }
                ),
                @UniqueConstraint(
                        name = "uk_learning_path_course_order",
                        columnNames = {
                                "path_id",
                                "course_order"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_learning_path_course_path",
                        columnList = "path_id"
                ),
                @Index(
                        name = "idx_learning_path_course_course",
                        columnList = "course_id"
                ),
                @Index(
                        name = "idx_learning_path_course_order",
                        columnList = "path_id, course_order"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningPathCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "path_course_id")
    private UUID pathCourseId;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "path_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_learning_path_course_path"
            )
    )
    private LearningPath learningPath;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "course_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_learning_path_course_course"
            )
    )
    private Course course;

    @Column(
            name = "course_order",
            nullable = false
    )
    private Integer courseOrder;

    @Column(
            name = "required_for_completion",
            nullable = false
    )
    private Boolean requiredForCompletion;

    @Column(
            name = "unlock_after_previous",
            nullable = false
    )
    private Boolean unlockAfterPrevious;

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

        if (requiredForCompletion == null) {
            requiredForCompletion = true;
        }

        if (unlockAfterPrevious == null) {
            unlockAfterPrevious = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}