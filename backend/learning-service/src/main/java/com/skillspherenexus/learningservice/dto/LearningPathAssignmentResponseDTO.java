package com.skillspherenexus.learningservice.dto;

import com.skillspherenexus.learningservice.enums.LearningPathAssignmentSource;
import com.skillspherenexus.learningservice.enums.LearningPathAssignmentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningPathAssignmentResponseDTO {

    private UUID assignmentId;

    private UUID pathId;

    private String pathCode;

    private String pathTitle;

    private UUID learnerId;

    private LearningPathAssignmentStatus status;

    private LearningPathAssignmentSource assignmentSource;

    private UUID assignedByUserId;

    private BigDecimal progressPercentage;

    private Integer currentCourseOrder;

    private Long totalCourses;

    private Long completedCourses;

    private Boolean overdue;

    private LocalDateTime assignedAt;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private LocalDateTime cancelledAt;

    private LocalDateTime dueAt;

    private LocalDateTime lastAccessedAt;

    private String cancellationReason;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}