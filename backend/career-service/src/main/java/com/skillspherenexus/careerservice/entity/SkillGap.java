package com.skillspherenexus.careerservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "skill_gaps")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class SkillGap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "career_plan_id", nullable = false)
    private CareerPlan careerPlan;

    @Column(nullable = false)
    private String skillName;

    @Column(nullable = false)
    private Integer currentLevel;

    @Column(nullable = false)
    private Integer requiredLevel;

    private Integer gapLevel;

    @Column(columnDefinition = "TEXT")
    private String trainingPlan;

    private LocalDateTime identifiedDate;

    private LocalDateTime filledDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (identifiedDate == null) {
            identifiedDate = LocalDateTime.now();
        }
        if (gapLevel == null && currentLevel != null && requiredLevel != null) {
            gapLevel = requiredLevel - currentLevel;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (gapLevel == null && currentLevel != null && requiredLevel != null) {
            gapLevel = requiredLevel - currentLevel;
        }
    }
}
