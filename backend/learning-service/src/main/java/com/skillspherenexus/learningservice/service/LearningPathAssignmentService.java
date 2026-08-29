package com.skillspherenexus.learningservice.service;

import com.skillspherenexus.learningservice.dto.LearningPathAssignmentRequestDTO;
import com.skillspherenexus.learningservice.dto.LearningPathAssignmentResponseDTO;
import com.skillspherenexus.learningservice.enums.LearningPathAssignmentStatus;

import java.util.List;
import java.util.UUID;

public interface LearningPathAssignmentService {

    LearningPathAssignmentResponseDTO assignLearningPath(
            UUID pathId,
            LearningPathAssignmentRequestDTO request
    );

    LearningPathAssignmentResponseDTO getAssignmentById(
            UUID assignmentId
    );

    LearningPathAssignmentResponseDTO getAssignment(
            UUID pathId,
            UUID learnerId
    );

    List<LearningPathAssignmentResponseDTO>
    getAssignmentsByLearner(
            UUID learnerId
    );

    List<LearningPathAssignmentResponseDTO>
    getAssignmentsByPath(
            UUID pathId
    );

    List<LearningPathAssignmentResponseDTO>
    getAssignmentsByStatus(
            LearningPathAssignmentStatus status
    );

    List<LearningPathAssignmentResponseDTO>
    getLearnerAssignmentsByStatus(
            UUID learnerId,
            LearningPathAssignmentStatus status
    );

    LearningPathAssignmentResponseDTO startLearningPath(
            UUID assignmentId
    );

    LearningPathAssignmentResponseDTO refreshProgress(
            UUID assignmentId
    );

    LearningPathAssignmentResponseDTO completeLearningPath(
            UUID assignmentId
    );

    LearningPathAssignmentResponseDTO cancelAssignment(
            UUID assignmentId,
            String cancellationReason
    );

    LearningPathAssignmentResponseDTO reactivateAssignment(
            UUID assignmentId
    );

    long countAssignmentsByLearner(
            UUID learnerId
    );

    long countAssignmentsByPath(
            UUID pathId
    );

    long countAssignmentsByPathAndStatus(
            UUID pathId,
            LearningPathAssignmentStatus status
    );
}