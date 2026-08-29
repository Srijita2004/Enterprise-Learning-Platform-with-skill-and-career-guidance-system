package com.skillspherenexus.learningservice.repository;

import com.skillspherenexus.learningservice.entity.LearningPathAssignment;
import com.skillspherenexus.learningservice.enums.LearningPathAssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LearningPathAssignmentRepository
        extends JpaRepository<LearningPathAssignment, UUID> {

    Optional<LearningPathAssignment>
    findByLearningPathPathIdAndLearnerId(
            UUID pathId,
            UUID learnerId
    );

    boolean existsByLearningPathPathIdAndLearnerId(
            UUID pathId,
            UUID learnerId
    );

    List<LearningPathAssignment>
    findAllByLearnerIdOrderByAssignedAtDesc(
            UUID learnerId
    );

    List<LearningPathAssignment>
    findAllByLearningPathPathIdOrderByAssignedAtDesc(
            UUID pathId
    );

    List<LearningPathAssignment>
    findAllByStatusOrderByAssignedAtDesc(
            LearningPathAssignmentStatus status
    );

    List<LearningPathAssignment>
    findAllByLearnerIdAndStatusOrderByAssignedAtDesc(
            UUID learnerId,
            LearningPathAssignmentStatus status
    );

    List<LearningPathAssignment>
    findAllByLearningPathPathIdAndStatusOrderByAssignedAtDesc(
            UUID pathId,
            LearningPathAssignmentStatus status
    );

    List<LearningPathAssignment>
    findAllByStatusAndDueAtBefore(
            LearningPathAssignmentStatus status,
            LocalDateTime currentTime
    );

    long countByLearnerId(
            UUID learnerId
    );

    long countByLearningPathPathId(
            UUID pathId
    );

    long countByLearningPathPathIdAndStatus(
            UUID pathId,
            LearningPathAssignmentStatus status
    );

    long countByLearnerIdAndStatus(
            UUID learnerId,
            LearningPathAssignmentStatus status
    );
}