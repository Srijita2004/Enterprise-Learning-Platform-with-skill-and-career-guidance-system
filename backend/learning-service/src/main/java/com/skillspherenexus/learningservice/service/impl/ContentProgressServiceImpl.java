package com.skillspherenexus.learningservice.service.impl;

import com.skillspherenexus.learningservice.dto.ContentProgressResponseDTO;
import com.skillspherenexus.learningservice.dto.ContentProgressUpdateRequestDTO;
import com.skillspherenexus.learningservice.dto.CourseProgressSummaryResponseDTO;
import com.skillspherenexus.learningservice.entity.ContentProgress;
import com.skillspherenexus.learningservice.entity.Course;
import com.skillspherenexus.learningservice.entity.CourseContent;
import com.skillspherenexus.learningservice.entity.CourseModule;
import com.skillspherenexus.learningservice.entity.Enrollment;
import com.skillspherenexus.learningservice.enums.ContentProgressStatus;
import com.skillspherenexus.learningservice.enums.EnrollmentStatus;
import com.skillspherenexus.learningservice.exception.ResourceNotFoundException;
import com.skillspherenexus.learningservice.repository.ContentProgressRepository;
import com.skillspherenexus.learningservice.repository.CourseContentRepository;
import com.skillspherenexus.learningservice.repository.EnrollmentRepository;
import com.skillspherenexus.learningservice.service.ContentProgressService;
import com.skillspherenexus.learningservice.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ContentProgressServiceImpl
        implements ContentProgressService {

    private final ContentProgressRepository
            contentProgressRepository;

    private final CourseContentRepository
            courseContentRepository;

    private final EnrollmentRepository
            enrollmentRepository;

    private final EnrollmentService
            enrollmentService;

    @Override
    public ContentProgressResponseDTO updateContentProgress(
            UUID enrollmentId,
            UUID contentId,
            ContentProgressUpdateRequestDTO request
    ) {
        Enrollment enrollment =
                getEnrollmentOrThrow(enrollmentId);

        validateEnrollmentAccess(enrollment);

        CourseContent content =
                getCourseContentOrThrow(
                        contentId,
                        enrollment.getCourse().getCourseId()
                );

        validateContentForProgress(content);

        validateLastPosition(
                content,
                request.getLastPositionSeconds()
        );

        ContentProgress progress =
                contentProgressRepository
                        .findByEnrollmentEnrollmentIdAndContentContentId(
                                enrollmentId,
                                contentId
                        )
                        .orElseGet(() ->
                                createInitialProgress(
                                        enrollment,
                                        content
                                )
                        );

        LocalDateTime currentTime =
                LocalDateTime.now();

        int requestedPercentage =
                request.getProgressPercentage();

        int existingPercentage =
                progress.getProgressPercentage() == null
                        ? 0
                        : progress.getProgressPercentage();

        int finalPercentage =
                Math.max(
                        existingPercentage,
                        requestedPercentage
                );

        Long additionalTime =
                request.getAdditionalTimeSpentSeconds() == null
                        ? 0L
                        : request.getAdditionalTimeSpentSeconds();

        Long currentTimeSpent =
                progress.getTimeSpentSeconds() == null
                        ? 0L
                        : progress.getTimeSpentSeconds();

        Long lastPosition =
                request.getLastPositionSeconds() == null
                        ? progress.getLastPositionSeconds()
                        : request.getLastPositionSeconds();

        progress.setProgressPercentage(
                finalPercentage
        );

        progress.setLastPositionSeconds(
                lastPosition == null
                        ? 0L
                        : lastPosition
        );

        progress.setTimeSpentSeconds(
                currentTimeSpent + additionalTime
        );

        progress.setStatus(
                determineProgressStatus(finalPercentage)
        );

        if (progress.getStartedAt() == null
                && hasLearningStarted(
                finalPercentage,
                progress.getLastPositionSeconds(),
                additionalTime
        )) {
            progress.setStartedAt(currentTime);
        }

        if (finalPercentage == 100) {
            if (progress.getCompletedAt() == null) {
                progress.setCompletedAt(currentTime);
            }
        } else {
            progress.setCompletedAt(null);
        }

        progress.setLastAccessedAt(currentTime);
        enrollment.setLastAccessedAt(currentTime);

        enrollmentRepository.save(enrollment);

        ContentProgress savedProgress =
                contentProgressRepository.save(progress);

        return mapToResponse(savedProgress);
    }

    @Override
    @Transactional(readOnly = true)
    public ContentProgressResponseDTO getContentProgress(
            UUID enrollmentId,
            UUID contentId
    ) {
        Enrollment enrollment =
                getEnrollmentOrThrow(enrollmentId);

        CourseContent content =
                getCourseContentOrThrow(
                        contentId,
                        enrollment.getCourse().getCourseId()
                );

        validateContentForProgress(content);

        return contentProgressRepository
                .findByEnrollmentEnrollmentIdAndContentContentId(
                        enrollmentId,
                        contentId
                )
                .map(this::mapToResponse)
                .orElseGet(() ->
                        mapNotStartedResponse(
                                enrollment,
                                content
                        )
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContentProgressResponseDTO>
    getProgressByEnrollment(
            UUID enrollmentId
    ) {
        Enrollment enrollment =
                getEnrollmentOrThrow(enrollmentId);

        List<CourseContent> publishedContents =
                getPublishedContentsForEnrollment(enrollment);

        Map<UUID, ContentProgress> progressMap =
                getProgressMap(enrollmentId);

        return publishedContents.stream()
                .map(content -> {
                    ContentProgress progress =
                            progressMap.get(
                                    content.getContentId()
                            );

                    if (progress == null) {
                        return mapNotStartedResponse(
                                enrollment,
                                content
                        );
                    }

                    return mapToResponse(progress);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContentProgressResponseDTO>
    getProgressByEnrollmentAndStatus(
            UUID enrollmentId,
            ContentProgressStatus status
    ) {
        if (status == null) {
            throw new IllegalArgumentException(
                    "Content progress status is required"
            );
        }

        return getProgressByEnrollment(enrollmentId)
                .stream()
                .filter(progress ->
                        progress.getStatus() == status
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CourseProgressSummaryResponseDTO
    getCourseProgressSummary(
            UUID enrollmentId
    ) {
        Enrollment enrollment =
                getEnrollmentOrThrow(enrollmentId);

        List<CourseContent> publishedContents =
                getPublishedContentsForEnrollment(enrollment);

        Map<UUID, ContentProgress> progressMap =
                getProgressMap(enrollmentId);

        long totalPublishedContents =
                publishedContents.size();

        long completedContents = 0L;
        long inProgressContents = 0L;
        long notStartedContents = 0L;

        long totalMandatoryContents = 0L;
        long completedMandatoryContents = 0L;

        long totalTimeSpentSeconds = 0L;

        long totalProgressPercentage = 0L;
        long totalMandatoryProgressPercentage = 0L;

        LocalDateTime latestAccessTime =
                enrollment.getLastAccessedAt();

        for (CourseContent content : publishedContents) {

            ContentProgress progress =
                    progressMap.get(
                            content.getContentId()
                    );

            int percentage = 0;
            ContentProgressStatus progressStatus =
                    ContentProgressStatus.NOT_STARTED;

            if (progress != null) {
                percentage =
                        progress.getProgressPercentage() == null
                                ? 0
                                : progress.getProgressPercentage();

                progressStatus =
                        progress.getStatus() == null
                                ? determineProgressStatus(
                                percentage
                        )
                                : progress.getStatus();

                totalTimeSpentSeconds +=
                        progress.getTimeSpentSeconds() == null
                                ? 0L
                                : progress.getTimeSpentSeconds();

                latestAccessTime =
                        findLatestTime(
                                latestAccessTime,
                                progress.getLastAccessedAt()
                        );
            }

            totalProgressPercentage += percentage;

            if (progressStatus
                    == ContentProgressStatus.COMPLETED) {
                completedContents++;
            } else if (progressStatus
                    == ContentProgressStatus.IN_PROGRESS) {
                inProgressContents++;
            } else {
                notStartedContents++;
            }

            if (Boolean.TRUE.equals(
                    content.getMandatory()
            )) {
                totalMandatoryContents++;

                totalMandatoryProgressPercentage +=
                        percentage;

                if (progressStatus
                        == ContentProgressStatus.COMPLETED) {
                    completedMandatoryContents++;
                }
            }
        }

        double overallProgressPercentage =
                calculatePercentageAverage(
                        totalProgressPercentage,
                        totalPublishedContents
                );

        double mandatoryProgressPercentage =
                calculatePercentageAverage(
                        totalMandatoryProgressPercentage,
                        totalMandatoryContents
                );

        boolean allMandatoryContentsCompleted =
                totalPublishedContents > 0
                        && completedMandatoryContents
                        == totalMandatoryContents;

        Course course =
                enrollment.getCourse();

        return CourseProgressSummaryResponseDTO.builder()
                .enrollmentId(
                        enrollment.getEnrollmentId()
                )
                .learnerId(
                        enrollment.getLearnerId()
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
                .enrollmentStatus(
                        enrollment.getStatus()
                )
                .totalPublishedContents(
                        totalPublishedContents
                )
                .completedContents(
                        completedContents
                )
                .inProgressContents(
                        inProgressContents
                )
                .notStartedContents(
                        notStartedContents
                )
                .totalMandatoryContents(
                        totalMandatoryContents
                )
                .completedMandatoryContents(
                        completedMandatoryContents
                )
                .overallProgressPercentage(
                        overallProgressPercentage
                )
                .mandatoryProgressPercentage(
                        mandatoryProgressPercentage
                )
                .totalTimeSpentSeconds(
                        totalTimeSpentSeconds
                )
                .lastAccessedAt(
                        latestAccessTime
                )
                .allMandatoryContentsCompleted(
                        allMandatoryContentsCompleted
                )
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean areAllMandatoryContentsCompleted(
            UUID enrollmentId
    ) {
        CourseProgressSummaryResponseDTO summary =
                getCourseProgressSummary(enrollmentId);

        return Boolean.TRUE.equals(
                summary.getAllMandatoryContentsCompleted()
        );
    }

    private Enrollment getEnrollmentOrThrow(
            UUID enrollmentId
    ) {
        return enrollmentRepository
                .findById(enrollmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Enrollment not found with ID: "
                                        + enrollmentId
                        )
                );
    }

    private CourseContent getCourseContentOrThrow(
            UUID contentId,
            UUID courseId
    ) {
        return courseContentRepository
                .findByContentIdAndCourseModuleCourseCourseId(
                        contentId,
                        courseId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Content not found with ID: "
                                        + contentId
                                        + " in course: "
                                        + courseId
                        )
                );
    }

    private void validateEnrollmentAccess(
            Enrollment enrollment
    ) {
        if (enrollment.getStatus()
                != EnrollmentStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Only an active enrollment can update progress"
            );
        }

        boolean accessAllowed =
                enrollmentService.hasActiveCourseAccess(
                        enrollment.getLearnerId(),
                        enrollment.getCourse().getCourseId()
                );

        if (!accessAllowed) {
            throw new IllegalArgumentException(
                    "Learner does not have active access "
                            + "to this course"
            );
        }
    }

    private void validateContentForProgress(
            CourseContent content
    ) {
        CourseModule courseModule =
                content.getCourseModule();

        if (!Boolean.TRUE.equals(
                courseModule.getPublished()
        )) {
            throw new IllegalArgumentException(
                    "Progress cannot be recorded for "
                            + "an unpublished module"
            );
        }

        if (!Boolean.TRUE.equals(
                content.getPublished()
        )) {
            throw new IllegalArgumentException(
                    "Progress cannot be recorded for "
                            + "unpublished content"
            );
        }

        LocalDateTime currentTime =
                LocalDateTime.now();

        if (content.getAvailableFrom() != null
                && currentTime.isBefore(
                content.getAvailableFrom()
        )) {
            throw new IllegalArgumentException(
                    "Content is not available yet"
            );
        }

        if (content.getAvailableUntil() != null
                && currentTime.isAfter(
                content.getAvailableUntil()
        )) {
            throw new IllegalArgumentException(
                    "Content availability has expired"
            );
        }
    }

    private void validateLastPosition(
            CourseContent content,
            Long lastPositionSeconds
    ) {
        if (lastPositionSeconds == null) {
            return;
        }

        Integer durationMinutes =
                content.getDurationMinutes();

        if (durationMinutes == null
                || durationMinutes <= 0) {
            return;
        }

        long maximumPositionSeconds =
                durationMinutes.longValue() * 60L;

        if (lastPositionSeconds
                > maximumPositionSeconds) {
            throw new IllegalArgumentException(
                    "Last position cannot exceed content duration of "
                            + maximumPositionSeconds
                            + " seconds"
            );
        }
    }

    private ContentProgress createInitialProgress(
            Enrollment enrollment,
            CourseContent content
    ) {
        return ContentProgress.builder()
                .enrollment(enrollment)
                .content(content)
                .status(
                        ContentProgressStatus.NOT_STARTED
                )
                .progressPercentage(0)
                .lastPositionSeconds(0L)
                .timeSpentSeconds(0L)
                .build();
    }

    private List<CourseContent>
    getPublishedContentsForEnrollment(
            Enrollment enrollment
    ) {
        return courseContentRepository
                .findByCourseModuleCourseCourseIdAndCourseModulePublishedTrueAndPublishedTrueOrderByCourseModuleModuleOrderAscContentOrderAsc(
                        enrollment.getCourse().getCourseId()
                );
    }

    private Map<UUID, ContentProgress> getProgressMap(
            UUID enrollmentId
    ) {
        List<ContentProgress> progressRecords =
                contentProgressRepository
                        .findAllByEnrollmentEnrollmentIdOrderByContentCourseModuleModuleOrderAscContentContentOrderAsc(
                                enrollmentId
                        );

        Map<UUID, ContentProgress> progressMap =
                new HashMap<>();

        for (ContentProgress progress
                : progressRecords) {
            progressMap.put(
                    progress.getContent().getContentId(),
                    progress
            );
        }

        return progressMap;
    }

    private ContentProgressStatus determineProgressStatus(
            int progressPercentage
    ) {
        if (progressPercentage >= 100) {
            return ContentProgressStatus.COMPLETED;
        }

        if (progressPercentage > 0) {
            return ContentProgressStatus.IN_PROGRESS;
        }

        return ContentProgressStatus.NOT_STARTED;
    }

    private boolean hasLearningStarted(
            int progressPercentage,
            Long lastPositionSeconds,
            Long additionalTimeSpentSeconds
    ) {
        return progressPercentage > 0
                || (lastPositionSeconds != null
                && lastPositionSeconds > 0)
                || (additionalTimeSpentSeconds != null
                && additionalTimeSpentSeconds > 0);
    }

    private double calculatePercentageAverage(
            long totalPercentage,
            long totalItems
    ) {
        if (totalItems == 0) {
            return 0.0;
        }

        return BigDecimal
                .valueOf(totalPercentage)
                .divide(
                        BigDecimal.valueOf(totalItems),
                        2,
                        RoundingMode.HALF_UP
                )
                .doubleValue();
    }

    private LocalDateTime findLatestTime(
            LocalDateTime firstTime,
            LocalDateTime secondTime
    ) {
        if (firstTime == null) {
            return secondTime;
        }

        if (secondTime == null) {
            return firstTime;
        }

        return firstTime.isAfter(secondTime)
                ? firstTime
                : secondTime;
    }

    private ContentProgressResponseDTO mapToResponse(
            ContentProgress progress
    ) {
        Enrollment enrollment =
                progress.getEnrollment();

        CourseContent content =
                progress.getContent();

        CourseModule courseModule =
                content.getCourseModule();

        Course course =
                courseModule.getCourse();

        return ContentProgressResponseDTO.builder()
                .progressId(
                        progress.getProgressId()
                )
                .enrollmentId(
                        enrollment.getEnrollmentId()
                )
                .learnerId(
                        enrollment.getLearnerId()
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
                .moduleId(
                        courseModule.getModuleId()
                )
                .moduleTitle(
                        courseModule.getTitle()
                )
                .moduleOrder(
                        courseModule.getModuleOrder()
                )
                .contentId(
                        content.getContentId()
                )
                .contentTitle(
                        content.getTitle()
                )
                .contentType(
                        content.getContentType()
                )
                .contentOrder(
                        content.getContentOrder()
                )
                .durationMinutes(
                        content.getDurationMinutes()
                )
                .mandatory(
                        content.getMandatory()
                )
                .status(
                        progress.getStatus()
                )
                .progressPercentage(
                        progress.getProgressPercentage()
                )
                .lastPositionSeconds(
                        progress.getLastPositionSeconds()
                )
                .timeSpentSeconds(
                        progress.getTimeSpentSeconds()
                )
                .startedAt(
                        progress.getStartedAt()
                )
                .completedAt(
                        progress.getCompletedAt()
                )
                .lastAccessedAt(
                        progress.getLastAccessedAt()
                )
                .createdAt(
                        progress.getCreatedAt()
                )
                .updatedAt(
                        progress.getUpdatedAt()
                )
                .build();
    }

    private ContentProgressResponseDTO
    mapNotStartedResponse(
            Enrollment enrollment,
            CourseContent content
    ) {
        CourseModule courseModule =
                content.getCourseModule();

        Course course =
                courseModule.getCourse();

        return ContentProgressResponseDTO.builder()
                .progressId(null)
                .enrollmentId(
                        enrollment.getEnrollmentId()
                )
                .learnerId(
                        enrollment.getLearnerId()
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
                .moduleId(
                        courseModule.getModuleId()
                )
                .moduleTitle(
                        courseModule.getTitle()
                )
                .moduleOrder(
                        courseModule.getModuleOrder()
                )
                .contentId(
                        content.getContentId()
                )
                .contentTitle(
                        content.getTitle()
                )
                .contentType(
                        content.getContentType()
                )
                .contentOrder(
                        content.getContentOrder()
                )
                .durationMinutes(
                        content.getDurationMinutes()
                )
                .mandatory(
                        content.getMandatory()
                )
                .status(
                        ContentProgressStatus.NOT_STARTED
                )
                .progressPercentage(0)
                .lastPositionSeconds(0L)
                .timeSpentSeconds(0L)
                .startedAt(null)
                .completedAt(null)
                .lastAccessedAt(null)
                .createdAt(null)
                .updatedAt(null)
                .build();
    }
}