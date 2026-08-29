package com.skillspherenexus.careerservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "career_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CareerPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long employeeId;

    @Column(nullable = false)
    private String employeeName;

    @Column(name = "curr_role", nullable = false)
    private String currentRole;

    @Column(nullable = false)
    private String targetRole;

    @Column(nullable = false)
    private Integer progressPercentage;

    private String mentorId;

    private String mentorName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CareerPlanStatus status;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDateTime startDate;

    private LocalDateTime targetCompletionDate;

    private LocalDateTime completionDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private Long createdBy;

    private Long updatedBy;

    @Enumerated(EnumType.STRING)
    private CareerPlanStatus previousStatus;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = CareerPlanStatus.ACTIVE;
        }
        if (progressPercentage == null) {
            progressPercentage = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
