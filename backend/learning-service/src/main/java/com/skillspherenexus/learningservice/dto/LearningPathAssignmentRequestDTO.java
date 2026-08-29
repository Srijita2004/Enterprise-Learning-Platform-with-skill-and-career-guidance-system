package com.skillspherenexus.learningservice.dto;

import com.skillspherenexus.learningservice.enums.LearningPathAssignmentSource;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningPathAssignmentRequestDTO {

    @NotNull(message = "Learner ID is required")
    private UUID learnerId;

    private LearningPathAssignmentSource assignmentSource;

    private UUID assignedByUserId;

    @Future(message = "Due date must be in the future")
    private LocalDateTime dueAt;
}