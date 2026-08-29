package com.skillspherenexus.careerservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "training_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long employeeId;

    @Column(nullable = false)
    private String courseName;

    @Column(nullable = false)
    private String courseId;

    private Integer score;

    private Integer completionPercentage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrainingStatus status;

    private LocalDateTime enrollmentDate;

    private LocalDateTime completionDate;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    private Double skillImprovement;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = TrainingStatus.ENROLLED;
        }
        if (enrollmentDate == null) {
            enrollmentDate = LocalDateTime.now();
        }
        if (completionPercentage == null) {
            completionPercentage = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
