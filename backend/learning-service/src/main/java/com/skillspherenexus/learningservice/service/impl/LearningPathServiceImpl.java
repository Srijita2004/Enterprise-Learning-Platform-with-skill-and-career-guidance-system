package com.skillspherenexus.learningservice.service.impl;

import com.skillspherenexus.learningservice.dto.LearningPathCourseRequestDTO;
import com.skillspherenexus.learningservice.dto.LearningPathCourseResponseDTO;
import com.skillspherenexus.learningservice.dto.LearningPathCreateRequestDTO;
import com.skillspherenexus.learningservice.dto.LearningPathResponseDTO;
import com.skillspherenexus.learningservice.dto.LearningPathUpdateRequestDTO;
import com.skillspherenexus.learningservice.entity.Course;
import com.skillspherenexus.learningservice.entity.LearningPath;
import com.skillspherenexus.learningservice.entity.LearningPathCourse;
import com.skillspherenexus.learningservice.enums.CourseStatus;
import com.skillspherenexus.learningservice.enums.LearningPathStatus;
import com.skillspherenexus.learningservice.exception.ResourceNotFoundException;
import com.skillspherenexus.learningservice.repository.CourseRepository;
import com.skillspherenexus.learningservice.repository.LearningPathAssignmentRepository;
import com.skillspherenexus.learningservice.repository.LearningPathCourseRepository;
import com.skillspherenexus.learningservice.repository.LearningPathRepository;
import com.skillspherenexus.learningservice.service.LearningPathService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class LearningPathServiceImpl
        implements LearningPathService {

    private final LearningPathRepository learningPathRepository;

    private final LearningPathCourseRepository
            learningPathCourseRepository;

    private final LearningPathAssignmentRepository
            learningPathAssignmentRepository;

    private final CourseRepository courseRepository;

    @Override
    public LearningPathResponseDTO createLearningPath(
            LearningPathCreateRequestDTO request
    ) {
        String normalizedPathCode =
                normalizePathCode(request.getPathCode());

        if (learningPathRepository
                .existsByPathCodeIgnoreCase(
                        normalizedPathCode
                )) {
            throw new IllegalArgumentException(
                    "Learning path code already exists: "
                            + normalizedPathCode
            );
        }

        LearningPath learningPath =
                LearningPath.builder()
                        .pathCode(normalizedPathCode)
                        .title(
                                normalizeRequiredText(
                                        request.getTitle(),
                                        "Learning path title"
                                )
                        )
                        .description(
                                normalizeOptionalText(
                                        request.getDescription()
                                )
                        )
                        .category(
                                normalizeRequiredText(
                                        request.getCategory(),
                                        "Category"
                                )
                        )
                        .targetRole(
                                normalizeOptionalText(
                                        request.getTargetRole()
                                )
                        )
                        .level(request.getLevel())
                        .status(LearningPathStatus.DRAFT)
                        .estimatedDurationHours(
                                request.getEstimatedDurationHours()
                                        == null
                                        ? 0
                                        : request
                                        .getEstimatedDurationHours()
                        )
                        .createdByUserId(
                                request.getCreatedByUserId()
                        )
                        .build();

        LearningPath savedLearningPath =
                learningPathRepository.save(
                        learningPath
                );

        return mapToResponse(savedLearningPath);
    }

    @Override
    public LearningPathResponseDTO updateLearningPath(
            UUID pathId,
            LearningPathUpdateRequestDTO request
    ) {
        LearningPath learningPath =
                getLearningPathOrThrow(pathId);

        if (learningPath.getStatus()
                == LearningPathStatus.ARCHIVED) {
            throw new IllegalArgumentException(
                    "Archived learning path cannot be updated"
            );
        }

        learningPath.setTitle(
                normalizeRequiredText(
                        request.getTitle(),
                        "Learning path title"
                )
        );

        learningPath.setDescription(
                normalizeOptionalText(
                        request.getDescription()
                )
        );

        learningPath.setCategory(
                normalizeRequiredText(
                        request.getCategory(),
                        "Category"
                )
        );

        learningPath.setTargetRole(
                normalizeOptionalText(
                        request.getTargetRole()
                )
        );

        learningPath.setLevel(
                request.getLevel()
        );

        learningPath.setEstimatedDurationHours(
                request.getEstimatedDurationHours()
                        == null
                        ? 0
                        : request.getEstimatedDurationHours()
        );

        LearningPath updatedLearningPath =
                learningPathRepository.save(
                        learningPath
                );

        return mapToResponse(updatedLearningPath);
    }

    @Override
    @Transactional(readOnly = true)
    public LearningPathResponseDTO getLearningPathById(
            UUID pathId
    ) {
        return mapToResponse(
                getLearningPathOrThrow(pathId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public LearningPathResponseDTO getLearningPathByCode(
            String pathCode
    ) {
        String normalizedPathCode =
                normalizePathCode(pathCode);

        LearningPath learningPath =
                learningPathRepository
                        .findByPathCodeIgnoreCase(
                                normalizedPathCode
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Learning path not found "
                                                + "with code: "
                                                + normalizedPathCode
                                )
                        );

        return mapToResponse(learningPath);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearningPathResponseDTO>
    getAllLearningPaths() {
        return learningPathRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearningPathResponseDTO>
    getLearningPathsByStatus(
            LearningPathStatus status
    ) {
        if (status == null) {
            throw new IllegalArgumentException(
                    "Learning path status is required"
            );
        }

        return learningPathRepository
                .findAllByStatusOrderByCreatedAtDesc(
                        status
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearningPathResponseDTO>
    getLearningPathsByCategory(
            String category
    ) {
        String normalizedCategory =
                normalizeRequiredText(
                        category,
                        "Category"
                );

        return learningPathRepository
                .findAllByCategoryIgnoreCaseOrderByTitleAsc(
                        normalizedCategory
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearningPathResponseDTO>
    getLearningPathsByTargetRole(
            String targetRole
    ) {
        String normalizedTargetRole =
                normalizeRequiredText(
                        targetRole,
                        "Target role"
                );

        return learningPathRepository
                .findAllByTargetRoleIgnoreCaseOrderByTitleAsc(
                        normalizedTargetRole
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public LearningPathResponseDTO publishLearningPath(
            UUID pathId
    ) {
        LearningPath learningPath =
                getLearningPathOrThrow(pathId);

        if (learningPath.getStatus()
                == LearningPathStatus.PUBLISHED) {
            return mapToResponse(learningPath);
        }

        if (learningPath.getStatus()
                == LearningPathStatus.ARCHIVED) {
            throw new IllegalArgumentException(
                    "Archived learning path cannot be published"
            );
        }

        List<LearningPathCourse> pathCourses =
                learningPathCourseRepository
                        .findAllByLearningPathPathIdOrderByCourseOrderAsc(
                                pathId
                        );

        if (pathCourses.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one course must be added "
                            + "before publishing the learning path"
            );
        }

        boolean hasRequiredCourse =
                pathCourses.stream()
                        .anyMatch(pathCourse ->
                                Boolean.TRUE.equals(
                                        pathCourse
                                                .getRequiredForCompletion()
                                )
                        );

        if (!hasRequiredCourse) {
            throw new IllegalArgumentException(
                    "Learning path must contain at least "
                            + "one required course"
            );
        }

        for (LearningPathCourse pathCourse
                : pathCourses) {
            Course course =
                    pathCourse.getCourse();

            if (course.getStatus()
                    != CourseStatus.PUBLISHED) {
                throw new IllegalArgumentException(
                        "Course must be published before "
                                + "publishing the learning path: "
                                + course.getCourseCode()
                );
            }
        }

        learningPath.setStatus(
                LearningPathStatus.PUBLISHED
        );

        learningPath.setPublishedAt(
                LocalDateTime.now()
        );

        learningPath.setArchivedAt(null);

        LearningPath publishedLearningPath =
                learningPathRepository.save(
                        learningPath
                );

        return mapToResponse(publishedLearningPath);
    }

    @Override
    public LearningPathResponseDTO archiveLearningPath(
            UUID pathId
    ) {
        LearningPath learningPath =
                getLearningPathOrThrow(pathId);

        if (learningPath.getStatus()
                == LearningPathStatus.ARCHIVED) {
            return mapToResponse(learningPath);
        }

        learningPath.setStatus(
                LearningPathStatus.ARCHIVED
        );

        learningPath.setArchivedAt(
                LocalDateTime.now()
        );

        LearningPath archivedLearningPath =
                learningPathRepository.save(
                        learningPath
                );

        return mapToResponse(archivedLearningPath);
    }

    @Override
    public void deleteLearningPath(
            UUID pathId
    ) {
        LearningPath learningPath =
                getLearningPathOrThrow(pathId);

        if (learningPath.getStatus()
                != LearningPathStatus.DRAFT) {
            throw new IllegalArgumentException(
                    "Only draft learning paths can be deleted"
            );
        }

        long assignmentCount =
                learningPathAssignmentRepository
                        .countByLearningPathPathId(
                                pathId
                        );

        if (assignmentCount > 0) {
            throw new IllegalArgumentException(
                    "Learning path cannot be deleted because "
                            + "learner assignments already exist"
            );
        }

        learningPathCourseRepository
                .deleteAllByLearningPathPathId(
                        pathId
                );

        learningPathRepository.delete(
                learningPath
        );
    }

    @Override
    public LearningPathCourseResponseDTO
    addCourseToLearningPath(
            UUID pathId,
            LearningPathCourseRequestDTO request
    ) {
        LearningPath learningPath =
                getLearningPathOrThrow(pathId);

        validateDraftPathForCourseChange(
                learningPath
        );

        Course course =
                getCourseOrThrow(
                        request.getCourseId()
                );

        if (learningPathCourseRepository
                .existsByLearningPathPathIdAndCourseCourseId(
                        pathId,
                        course.getCourseId()
                )) {
            throw new IllegalArgumentException(
                    "Course already exists in this "
                            + "learning path"
            );
        }

        if (learningPathCourseRepository
                .existsByLearningPathPathIdAndCourseOrder(
                        pathId,
                        request.getCourseOrder()
                )) {
            throw new IllegalArgumentException(
                    "Another course already uses order: "
                            + request.getCourseOrder()
            );
        }

        LearningPathCourse learningPathCourse =
                LearningPathCourse.builder()
                        .learningPath(learningPath)
                        .course(course)
                        .courseOrder(
                                request.getCourseOrder()
                        )
                        .requiredForCompletion(
                                request
                                        .getRequiredForCompletion()
                                        == null
                                        ? true
                                        : request
                                        .getRequiredForCompletion()
                        )
                        .unlockAfterPrevious(
                                request
                                        .getUnlockAfterPrevious()
                                        == null
                                        ? true
                                        : request
                                        .getUnlockAfterPrevious()
                        )
                        .build();

        LearningPathCourse savedPathCourse =
                learningPathCourseRepository.save(
                        learningPathCourse
                );

        return mapCourseToResponse(
                savedPathCourse
        );
    }

    @Override
    public LearningPathCourseResponseDTO
    updateLearningPathCourse(
            UUID pathId,
            UUID pathCourseId,
            LearningPathCourseRequestDTO request
    ) {
        LearningPath learningPath =
                getLearningPathOrThrow(pathId);

        validateDraftPathForCourseChange(
                learningPath
        );

        LearningPathCourse learningPathCourse =
                getLearningPathCourseOrThrow(
                        pathCourseId
                );

        validateCourseBelongsToPath(
                learningPathCourse,
                pathId
        );

        Course course =
                getCourseOrThrow(
                        request.getCourseId()
                );

        boolean duplicateCourse =
                learningPathCourseRepository
                        .existsByLearningPathPathIdAndCourseCourseIdAndPathCourseIdNot(
                                pathId,
                                course.getCourseId(),
                                pathCourseId
                        );

        if (duplicateCourse) {
            throw new IllegalArgumentException(
                    "Course already exists in this "
                            + "learning path"
            );
        }

        boolean duplicateOrder =
                learningPathCourseRepository
                        .existsByLearningPathPathIdAndCourseOrderAndPathCourseIdNot(
                                pathId,
                                request.getCourseOrder(),
                                pathCourseId
                        );

        if (duplicateOrder) {
            throw new IllegalArgumentException(
                    "Another course already uses order: "
                            + request.getCourseOrder()
            );
        }

        learningPathCourse.setCourse(course);

        learningPathCourse.setCourseOrder(
                request.getCourseOrder()
        );

        learningPathCourse.setRequiredForCompletion(
                request.getRequiredForCompletion()
                        == null
                        ? true
                        : request.getRequiredForCompletion()
        );

        learningPathCourse.setUnlockAfterPrevious(
                request.getUnlockAfterPrevious()
                        == null
                        ? true
                        : request.getUnlockAfterPrevious()
        );

        LearningPathCourse updatedPathCourse =
                learningPathCourseRepository.save(
                        learningPathCourse
                );

        return mapCourseToResponse(
                updatedPathCourse
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearningPathCourseResponseDTO>
    getLearningPathCourses(
            UUID pathId
    ) {
        getLearningPathOrThrow(pathId);

        return learningPathCourseRepository
                .findAllByLearningPathPathIdOrderByCourseOrderAsc(
                        pathId
                )
                .stream()
                .map(this::mapCourseToResponse)
                .toList();
    }

    @Override
    public void removeCourseFromLearningPath(
            UUID pathId,
            UUID pathCourseId
    ) {
        LearningPath learningPath =
                getLearningPathOrThrow(pathId);

        validateDraftPathForCourseChange(
                learningPath
        );

        LearningPathCourse learningPathCourse =
                getLearningPathCourseOrThrow(
                        pathCourseId
                );

        validateCourseBelongsToPath(
                learningPathCourse,
                pathId
        );

        learningPathCourseRepository.delete(
                learningPathCourse
        );
    }

    private LearningPath getLearningPathOrThrow(
            UUID pathId
    ) {
        return learningPathRepository
                .findById(pathId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Learning path not found "
                                        + "with ID: "
                                        + pathId
                        )
                );
    }

    private Course getCourseOrThrow(
            UUID courseId
    ) {
        return courseRepository
                .findById(courseId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Course not found with ID: "
                                        + courseId
                        )
                );
    }

    private LearningPathCourse
    getLearningPathCourseOrThrow(
            UUID pathCourseId
    ) {
        return learningPathCourseRepository
                .findById(pathCourseId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Learning path course not found "
                                        + "with ID: "
                                        + pathCourseId
                        )
                );
    }

    private void validateDraftPathForCourseChange(
            LearningPath learningPath
    ) {
        if (learningPath.getStatus()
                != LearningPathStatus.DRAFT) {
            throw new IllegalArgumentException(
                    "Courses can only be changed while "
                            + "the learning path is in DRAFT status"
            );
        }
    }

    private void validateCourseBelongsToPath(
            LearningPathCourse learningPathCourse,
            UUID pathId
    ) {
        UUID actualPathId =
                learningPathCourse
                        .getLearningPath()
                        .getPathId();

        if (!actualPathId.equals(pathId)) {
            throw new IllegalArgumentException(
                    "Learning path course does not belong "
                            + "to the specified learning path"
            );
        }
    }

    private String normalizePathCode(
            String pathCode
    ) {
        return normalizeRequiredText(
                pathCode,
                "Learning path code"
        )
                .toUpperCase(Locale.ROOT)
                .replaceAll("\\s+", "-");
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

    private String normalizeOptionalText(
            String text
    ) {
        if (text == null) {
            return null;
        }

        String normalizedText =
                text.trim();

        return normalizedText.isEmpty()
                ? null
                : normalizedText;
    }

    private LearningPathResponseDTO mapToResponse(
            LearningPath learningPath
    ) {
        UUID pathId =
                learningPath.getPathId();

        long totalCourses =
                learningPathCourseRepository
                        .countByLearningPathPathId(
                                pathId
                        );

        long requiredCourses =
                learningPathCourseRepository
                        .countByLearningPathPathIdAndRequiredForCompletionTrue(
                                pathId
                        );

        long totalAssignments =
                learningPathAssignmentRepository
                        .countByLearningPathPathId(
                                pathId
                        );

        return LearningPathResponseDTO.builder()
                .pathId(pathId)
                .pathCode(
                        learningPath.getPathCode()
                )
                .title(
                        learningPath.getTitle()
                )
                .description(
                        learningPath.getDescription()
                )
                .category(
                        learningPath.getCategory()
                )
                .targetRole(
                        learningPath.getTargetRole()
                )
                .level(
                        learningPath.getLevel()
                )
                .status(
                        learningPath.getStatus()
                )
                .estimatedDurationHours(
                        learningPath
                                .getEstimatedDurationHours()
                )
                .totalCourses(totalCourses)
                .requiredCourses(requiredCourses)
                .totalAssignments(totalAssignments)
                .createdByUserId(
                        learningPath.getCreatedByUserId()
                )
                .publishedAt(
                        learningPath.getPublishedAt()
                )
                .archivedAt(
                        learningPath.getArchivedAt()
                )
                .createdAt(
                        learningPath.getCreatedAt()
                )
                .updatedAt(
                        learningPath.getUpdatedAt()
                )
                .build();
    }

    private LearningPathCourseResponseDTO
    mapCourseToResponse(
            LearningPathCourse learningPathCourse
    ) {
        Course course =
                learningPathCourse.getCourse();

        return LearningPathCourseResponseDTO.builder()
                .pathCourseId(
                        learningPathCourse
                                .getPathCourseId()
                )
                .pathId(
                        learningPathCourse
                                .getLearningPath()
                                .getPathId()
                )
                .courseId(
                        course.getCourseId()
                )
                .courseCode(
                        course.getCourseCode()
                )
                .courseTitle(
                        course.getTitle()
                )
                .courseOrder(
                        learningPathCourse
                                .getCourseOrder()
                )
                .requiredForCompletion(
                        learningPathCourse
                                .getRequiredForCompletion()
                )
                .unlockAfterPrevious(
                        learningPathCourse
                                .getUnlockAfterPrevious()
                )
                .createdAt(
                        learningPathCourse
                                .getCreatedAt()
                )
                .updatedAt(
                        learningPathCourse
                                .getUpdatedAt()
                )
                .build();
    }
}