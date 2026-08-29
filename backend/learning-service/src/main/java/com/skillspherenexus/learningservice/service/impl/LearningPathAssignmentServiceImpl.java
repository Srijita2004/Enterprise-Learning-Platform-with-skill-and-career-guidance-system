package com.skillspherenexus.learningservice.service.impl;

import com.skillspherenexus.learningservice.dto.LearningPathAssignmentRequestDTO;
import com.skillspherenexus.learningservice.dto.LearningPathAssignmentResponseDTO;
import com.skillspherenexus.learningservice.entity.LearningPath;
import com.skillspherenexus.learningservice.entity.LearningPathAssignment;
import com.skillspherenexus.learningservice.entity.LearningPathCourse;
import com.skillspherenexus.learningservice.enums.EnrollmentStatus;
import com.skillspherenexus.learningservice.enums.LearningPathAssignmentSource;
import com.skillspherenexus.learningservice.enums.LearningPathAssignmentStatus;
import com.skillspherenexus.learningservice.enums.LearningPathStatus;
import com.skillspherenexus.learningservice.exception.ResourceNotFoundException;
import com.skillspherenexus.learningservice.repository.LearningPathAssignmentRepository;
import com.skillspherenexus.learningservice.repository.LearningPathCourseRepository;
import com.skillspherenexus.learningservice.repository.LearningPathRepository;
import com.skillspherenexus.learningservice.service.LearningPathAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class LearningPathAssignmentServiceImpl
        implements LearningPathAssignmentService {

    private final LearningPathAssignmentRepository
            learningPathAssignmentRepository;

    private final LearningPathRepository
            learningPathRepository;

    private final LearningPathCourseRepository
            learningPathCourseRepository;

    @Override
    public LearningPathAssignmentResponseDTO assignLearningPath(
            UUID pathId,
            LearningPathAssignmentRequestDTO request
    ) {
        LearningPath learningPath =
                getLearningPathOrThrow(pathId);

        if (learningPath.getStatus()
                != LearningPathStatus.PUBLISHED) {
            throw new IllegalArgumentException(
                    "Only published learning paths can be assigned"
            );
        }

        if (request.getLearnerId() == null) {
            throw new IllegalArgumentException(
                    "Learner ID is required"
            );
        }

        long totalCourses =
                learningPathCourseRepository
                        .countByLearningPathPathId(pathId);

        if (totalCourses == 0) {
            throw new IllegalArgumentException(
                    "Learning path does not contain any courses"
            );
        }

        boolean assignmentExists =
                learningPathAssignmentRepository
                        .existsByLearningPathPathIdAndLearnerId(
                                pathId,
                                request.getLearnerId()
                        );

        if (assignmentExists) {
            throw new IllegalArgumentException(
                    "This learning path is already assigned "
                            + "to the learner"
            );
        }

        validateDueDate(request.getDueAt());

        LearningPathAssignmentSource assignmentSource =
                request.getAssignmentSource() == null
                        ? LearningPathAssignmentSource.SELF_ASSIGNED
                        : request.getAssignmentSource();

        List<LearningPathCourse> pathCourses =
                learningPathCourseRepository
                        .findAllByLearningPathPathIdOrderByCourseOrderAsc(
                                pathId
                        );

        Integer firstCourseOrder =
                pathCourses.isEmpty()
                        ? null
                        : pathCourses.get(0).getCourseOrder();

        LearningPathAssignment assignment =
                LearningPathAssignment.builder()
                        .learningPath(learningPath)
                        .learnerId(
                                request.getLearnerId()
                        )
                        .status(
                                LearningPathAssignmentStatus.ASSIGNED
                        )
                        .assignmentSource(
                                assignmentSource
                        )
                        .assignedByUserId(
                                request.getAssignedByUserId()
                        )
                        .progressPercentage(
                                BigDecimal.ZERO.setScale(
                                        2,
                                        RoundingMode.HALF_UP
                                )
                        )
                        .currentCourseOrder(
                                firstCourseOrder
                        )
                        .dueAt(
                                request.getDueAt()
                        )
                        .build();

        LearningPathAssignment savedAssignment =
                learningPathAssignmentRepository.save(
                        assignment
                );

        return mapToResponse(savedAssignment);
    }

    @Override
    @Transactional(readOnly = true)
    public LearningPathAssignmentResponseDTO getAssignmentById(
            UUID assignmentId
    ) {
        LearningPathAssignment assignment =
                getAssignmentOrThrow(assignmentId);

        return mapToResponse(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public LearningPathAssignmentResponseDTO getAssignment(
            UUID pathId,
            UUID learnerId
    ) {
        LearningPathAssignment assignment =
                learningPathAssignmentRepository
                        .findByLearningPathPathIdAndLearnerId(
                                pathId,
                                learnerId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Learning path assignment "
                                                + "not found for path "
                                                + pathId
                                                + " and learner "
                                                + learnerId
                                )
                        );

        return mapToResponse(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearningPathAssignmentResponseDTO>
    getAssignmentsByLearner(
            UUID learnerId
    ) {
        return learningPathAssignmentRepository
                .findAllByLearnerIdOrderByAssignedAtDesc(
                        learnerId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearningPathAssignmentResponseDTO>
    getAssignmentsByPath(
            UUID pathId
    ) {
        getLearningPathOrThrow(pathId);

        return learningPathAssignmentRepository
                .findAllByLearningPathPathIdOrderByAssignedAtDesc(
                        pathId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearningPathAssignmentResponseDTO>
    getAssignmentsByStatus(
            LearningPathAssignmentStatus status
    ) {
        validateStatus(status);

        return learningPathAssignmentRepository
                .findAllByStatusOrderByAssignedAtDesc(
                        status
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearningPathAssignmentResponseDTO>
    getLearnerAssignmentsByStatus(
            UUID learnerId,
            LearningPathAssignmentStatus status
    ) {
        validateStatus(status);

        return learningPathAssignmentRepository
                .findAllByLearnerIdAndStatusOrderByAssignedAtDesc(
                        learnerId,
                        status
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public LearningPathAssignmentResponseDTO startLearningPath(
            UUID assignmentId
    ) {
        LearningPathAssignment assignment =
                getAssignmentOrThrow(assignmentId);

        if (assignment.getStatus()
                == LearningPathAssignmentStatus.COMPLETED) {
            return mapToResponse(assignment);
        }

        if (assignment.getStatus()
                == LearningPathAssignmentStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Cancelled assignment must be reactivated "
                            + "before starting"
            );
        }

        if (isPastDueDate(assignment.getDueAt())) {
            assignment.setStatus(
                    LearningPathAssignmentStatus.EXPIRED
            );

            assignment.setLastAccessedAt(
                    LocalDateTime.now()
            );

            LearningPathAssignment expiredAssignment =
                    learningPathAssignmentRepository.save(
                            assignment
                    );

            return mapToResponse(expiredAssignment);
        }

        if (assignment.getStatus()
                == LearningPathAssignmentStatus.EXPIRED) {
            throw new IllegalArgumentException(
                    "Expired assignment must be reactivated "
                            + "before starting"
            );
        }

        LocalDateTime currentTime =
                LocalDateTime.now();

        assignment.setStatus(
                LearningPathAssignmentStatus.IN_PROGRESS
        );

        if (assignment.getStartedAt() == null) {
            assignment.setStartedAt(currentTime);
        }

        assignment.setLastAccessedAt(currentTime);

        LearningPathAssignment startedAssignment =
                learningPathAssignmentRepository.save(
                        assignment
                );

        return mapToResponse(startedAssignment);
    }

    @Override
    public LearningPathAssignmentResponseDTO refreshProgress(
            UUID assignmentId
    ) {
        LearningPathAssignment assignment =
                getAssignmentOrThrow(assignmentId);

        if (assignment.getStatus()
                == LearningPathAssignmentStatus.CANCELLED) {
            return mapToResponse(assignment);
        }

        if (assignment.getStatus()
                == LearningPathAssignmentStatus.COMPLETED) {
            return mapToResponse(assignment);
        }

        if (assignment.getStatus()
                == LearningPathAssignmentStatus.EXPIRED) {
            return mapToResponse(assignment);
        }

        refreshAssignmentProgress(assignment);

        LearningPathAssignment updatedAssignment =
                learningPathAssignmentRepository.save(
                        assignment
                );

        return mapToResponse(updatedAssignment);
    }

    @Override
    public LearningPathAssignmentResponseDTO completeLearningPath(
            UUID assignmentId
    ) {
        LearningPathAssignment assignment =
                getAssignmentOrThrow(assignmentId);

        if (assignment.getStatus()
                == LearningPathAssignmentStatus.COMPLETED) {
            return mapToResponse(assignment);
        }

        if (assignment.getStatus()
                == LearningPathAssignmentStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Cancelled assignment cannot be completed"
            );
        }

        if (assignment.getStatus()
                == LearningPathAssignmentStatus.EXPIRED) {
            throw new IllegalArgumentException(
                    "Expired assignment must be reactivated "
                            + "before completion"
            );
        }

        UUID pathId =
                assignment
                        .getLearningPath()
                        .getPathId();

        UUID learnerId =
                assignment.getLearnerId();

        long requiredCourses =
                learningPathCourseRepository
                        .countByLearningPathPathIdAndRequiredForCompletionTrue(
                                pathId
                        );

        long completedRequiredCourses =
                learningPathCourseRepository
                        .countCompletedRequiredCoursesForLearner(
                                pathId,
                                learnerId,
                                EnrollmentStatus.COMPLETED
                        );

        if (requiredCourses == 0) {
            throw new IllegalArgumentException(
                    "Learning path does not contain "
                            + "any required courses"
            );
        }

        if (completedRequiredCourses < requiredCourses) {
            throw new IllegalArgumentException(
                    "All required courses must be completed "
                            + "before completing the learning path"
            );
        }

        markAssignmentCompleted(assignment);

        LearningPathAssignment completedAssignment =
                learningPathAssignmentRepository.save(
                        assignment
                );

        return mapToResponse(completedAssignment);
    }

    @Override
    public LearningPathAssignmentResponseDTO cancelAssignment(
            UUID assignmentId,
            String cancellationReason
    ) {
        LearningPathAssignment assignment =
                getAssignmentOrThrow(assignmentId);

        if (assignment.getStatus()
                == LearningPathAssignmentStatus.COMPLETED) {
            throw new IllegalArgumentException(
                    "Completed assignment cannot be cancelled"
            );
        }

        if (assignment.getStatus()
                == LearningPathAssignmentStatus.CANCELLED) {
            return mapToResponse(assignment);
        }

        String normalizedReason =
                normalizeRequiredText(
                        cancellationReason,
                        "Cancellation reason"
                );

        if (normalizedReason.length() > 500) {
            throw new IllegalArgumentException(
                    "Cancellation reason cannot exceed 500 characters"
            );
        }

        LocalDateTime currentTime =
                LocalDateTime.now();

        assignment.setStatus(
                LearningPathAssignmentStatus.CANCELLED
        );

        assignment.setCancelledAt(currentTime);

        assignment.setCancellationReason(
                normalizedReason
        );

        assignment.setLastAccessedAt(currentTime);

        LearningPathAssignment cancelledAssignment =
                learningPathAssignmentRepository.save(
                        assignment
                );

        return mapToResponse(cancelledAssignment);
    }

    @Override
    public LearningPathAssignmentResponseDTO reactivateAssignment(
            UUID assignmentId
    ) {
        LearningPathAssignment assignment =
                getAssignmentOrThrow(assignmentId);

        boolean cancelled =
                assignment.getStatus()
                        == LearningPathAssignmentStatus.CANCELLED;

        boolean expired =
                assignment.getStatus()
                        == LearningPathAssignmentStatus.EXPIRED;

        if (!cancelled && !expired) {
            throw new IllegalArgumentException(
                    "Only cancelled or expired assignments "
                            + "can be reactivated"
            );
        }

        if (isPastDueDate(assignment.getDueAt())) {
            assignment.setDueAt(null);
        }

        assignment.setStatus(
                LearningPathAssignmentStatus.ASSIGNED
        );

        assignment.setCancelledAt(null);

        assignment.setCancellationReason(null);

        assignment.setCompletedAt(null);

        assignment.setLastAccessedAt(
                LocalDateTime.now()
        );

        refreshAssignmentProgress(assignment);

        LearningPathAssignment reactivatedAssignment =
                learningPathAssignmentRepository.save(
                        assignment
                );

        return mapToResponse(reactivatedAssignment);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAssignmentsByLearner(
            UUID learnerId
    ) {
        return learningPathAssignmentRepository
                .countByLearnerId(learnerId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAssignmentsByPath(
            UUID pathId
    ) {
        getLearningPathOrThrow(pathId);

        return learningPathAssignmentRepository
                .countByLearningPathPathId(pathId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAssignmentsByPathAndStatus(
            UUID pathId,
            LearningPathAssignmentStatus status
    ) {
        getLearningPathOrThrow(pathId);

        validateStatus(status);

        return learningPathAssignmentRepository
                .countByLearningPathPathIdAndStatus(
                        pathId,
                        status
                );
    }

    private void refreshAssignmentProgress(
            LearningPathAssignment assignment
    ) {
        UUID pathId =
                assignment
                        .getLearningPath()
                        .getPathId();

        UUID learnerId =
                assignment.getLearnerId();

        long requiredCourses =
                learningPathCourseRepository
                        .countByLearningPathPathIdAndRequiredForCompletionTrue(
                                pathId
                        );

        long completedRequiredCourses =
                learningPathCourseRepository
                        .countCompletedRequiredCoursesForLearner(
                                pathId,
                                learnerId,
                                EnrollmentStatus.COMPLETED
                        );

        BigDecimal progressPercentage =
                calculateProgressPercentage(
                        completedRequiredCourses,
                        requiredCourses
                );

        assignment.setProgressPercentage(
                progressPercentage
        );

        Integer currentCourseOrder =
                learningPathCourseRepository
                        .findFirstIncompleteCourseOrder(
                                pathId,
                                learnerId,
                                EnrollmentStatus.COMPLETED
                        );

        assignment.setCurrentCourseOrder(
                currentCourseOrder
        );

        assignment.setLastAccessedAt(
                LocalDateTime.now()
        );

        if (requiredCourses > 0
                && completedRequiredCourses
                >= requiredCourses) {
            markAssignmentCompleted(assignment);
            return;
        }

        if (isPastDueDate(assignment.getDueAt())) {
            assignment.setStatus(
                    LearningPathAssignmentStatus.EXPIRED
            );
            return;
        }

        if (completedRequiredCourses > 0
                || assignment.getStartedAt() != null) {

            assignment.setStatus(
                    LearningPathAssignmentStatus.IN_PROGRESS
            );

            if (assignment.getStartedAt() == null) {
                assignment.setStartedAt(
                        LocalDateTime.now()
                );
            }

        } else {
            assignment.setStatus(
                    LearningPathAssignmentStatus.ASSIGNED
            );
        }
    }

    private void markAssignmentCompleted(
            LearningPathAssignment assignment
    ) {
        LocalDateTime currentTime =
                LocalDateTime.now();

        assignment.setStatus(
                LearningPathAssignmentStatus.COMPLETED
        );

        assignment.setProgressPercentage(
                BigDecimal.valueOf(100)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        )
        );

        assignment.setCurrentCourseOrder(null);

        if (assignment.getStartedAt() == null) {
            assignment.setStartedAt(currentTime);
        }

        if (assignment.getCompletedAt() == null) {
            assignment.setCompletedAt(currentTime);
        }

        assignment.setLastAccessedAt(currentTime);
    }

    private BigDecimal calculateProgressPercentage(
            long completedRequiredCourses,
            long totalRequiredCourses
    ) {
        if (totalRequiredCourses <= 0) {
            return BigDecimal.ZERO.setScale(
                    2,
                    RoundingMode.HALF_UP
            );
        }

        BigDecimal calculatedProgress =
                BigDecimal.valueOf(
                                completedRequiredCourses
                        )
                        .multiply(
                                BigDecimal.valueOf(100)
                        )
                        .divide(
                                BigDecimal.valueOf(
                                        totalRequiredCourses
                                ),
                                2,
                                RoundingMode.HALF_UP
                        );

        BigDecimal maximumProgress =
                BigDecimal.valueOf(100)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        return calculatedProgress.min(
                maximumProgress
        );
    }

    private LearningPathAssignment getAssignmentOrThrow(
            UUID assignmentId
    ) {
        return learningPathAssignmentRepository
                .findById(assignmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Learning path assignment not found "
                                        + "with ID: "
                                        + assignmentId
                        )
                );
    }

    private LearningPath getLearningPathOrThrow(
            UUID pathId
    ) {
        return learningPathRepository
                .findById(pathId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Learning path not found with ID: "
                                        + pathId
                        )
                );
    }

    private void validateDueDate(
            LocalDateTime dueAt
    ) {
        if (dueAt != null
                && !dueAt.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Due date must be in the future"
            );
        }
    }

    private boolean isPastDueDate(
            LocalDateTime dueAt
    ) {
        return dueAt != null
                && dueAt.isBefore(
                LocalDateTime.now()
        );
    }

    private void validateStatus(
            LearningPathAssignmentStatus status
    ) {
        if (status == null) {
            throw new IllegalArgumentException(
                    "Learning path assignment status is required"
            );
        }
    }

    private String normalizeRequiredText(
            String text,
            String fieldName
    ) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " is required"
            );
        }

        return text.trim();
    }

    private LearningPathAssignmentResponseDTO mapToResponse(
            LearningPathAssignment assignment
    ) {
        LearningPath learningPath =
                assignment.getLearningPath();

        UUID pathId =
                learningPath.getPathId();

        UUID learnerId =
                assignment.getLearnerId();

        long totalCourses =
                learningPathCourseRepository
                        .countByLearningPathPathId(
                                pathId
                        );

        long completedCourses =
                learningPathCourseRepository
                        .countCompletedCoursesForLearner(
                                pathId,
                                learnerId,
                                EnrollmentStatus.COMPLETED
                        );

        boolean overdue =
                isPastDueDate(
                        assignment.getDueAt()
                )
                        && assignment.getStatus()
                        != LearningPathAssignmentStatus.COMPLETED
                        && assignment.getStatus()
                        != LearningPathAssignmentStatus.CANCELLED;

        return LearningPathAssignmentResponseDTO.builder()
                .assignmentId(
                        assignment.getAssignmentId()
                )
                .pathId(pathId)
                .pathCode(
                        learningPath.getPathCode()
                )
                .pathTitle(
                        learningPath.getTitle()
                )
                .learnerId(learnerId)
                .status(
                        assignment.getStatus()
                )
                .assignmentSource(
                        assignment.getAssignmentSource()
                )
                .assignedByUserId(
                        assignment.getAssignedByUserId()
                )
                .progressPercentage(
                        assignment.getProgressPercentage()
                )
                .currentCourseOrder(
                        assignment.getCurrentCourseOrder()
                )
                .totalCourses(totalCourses)
                .completedCourses(completedCourses)
                .overdue(overdue)
                .assignedAt(
                        assignment.getAssignedAt()
                )
                .startedAt(
                        assignment.getStartedAt()
                )
                .completedAt(
                        assignment.getCompletedAt()
                )
                .cancelledAt(
                        assignment.getCancelledAt()
                )
                .dueAt(
                        assignment.getDueAt()
                )
                .lastAccessedAt(
                        assignment.getLastAccessedAt()
                )
                .cancellationReason(
                        assignment.getCancellationReason()
                )
                .createdAt(
                        assignment.getCreatedAt()
                )
                .updatedAt(
                        assignment.getUpdatedAt()
                )
                .build();
    }
}