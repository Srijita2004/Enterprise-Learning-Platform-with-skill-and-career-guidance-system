package com.skillspherenexus.learningservice.service.impl;

import com.skillspherenexus.learningservice.dto.AssessmentAttemptResponseDTO;
import com.skillspherenexus.learningservice.dto.AssessmentGradeRequestDTO;
import com.skillspherenexus.learningservice.dto.ContentProgressUpdateRequestDTO;
import com.skillspherenexus.learningservice.entity.AssessmentAttempt;
import com.skillspherenexus.learningservice.entity.Course;
import com.skillspherenexus.learningservice.entity.CourseContent;
import com.skillspherenexus.learningservice.entity.CourseModule;
import com.skillspherenexus.learningservice.entity.Enrollment;
import com.skillspherenexus.learningservice.enums.AssessmentStatus;
import com.skillspherenexus.learningservice.enums.ContentType;
import com.skillspherenexus.learningservice.enums.CourseStatus;
import com.skillspherenexus.learningservice.enums.EnrollmentStatus;
import com.skillspherenexus.learningservice.enums.PaymentStatus;
import com.skillspherenexus.learningservice.exception.ResourceNotFoundException;
import com.skillspherenexus.learningservice.repository.AssessmentAttemptRepository;
import com.skillspherenexus.learningservice.repository.CourseContentRepository;
import com.skillspherenexus.learningservice.repository.EnrollmentRepository;
import com.skillspherenexus.learningservice.service.AssessmentAttemptService;
import com.skillspherenexus.learningservice.service.ContentProgressService;
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
public class AssessmentAttemptServiceImpl
        implements AssessmentAttemptService {

    private final AssessmentAttemptRepository
            assessmentAttemptRepository;

    private final EnrollmentRepository
            enrollmentRepository;

    private final CourseContentRepository
            courseContentRepository;

    private final ContentProgressService
            contentProgressService;

    @Override
    public AssessmentAttemptResponseDTO startAssessmentAttempt(
            UUID enrollmentId,
            UUID contentId
    ) {
        Enrollment enrollment =
                getEnrollmentOrThrow(enrollmentId);

        validateEnrollmentAccess(enrollment);

        CourseContent content =
                getAssessmentContentOrThrow(
                        contentId,
                        enrollment.getCourse().getCourseId()
                );

        validateAssessmentAvailableForAttempt(content);

        assessmentAttemptRepository
                .findTopByEnrollmentEnrollmentIdAndContentContentIdOrderByAttemptNumberDesc(
                        enrollmentId,
                        contentId
                )
                .ifPresent(latestAttempt -> {
                    if (latestAttempt.getStatus()
                            == AssessmentStatus.IN_PROGRESS) {
                        throw new IllegalArgumentException(
                                "An assessment attempt is already in progress"
                        );
                    }

                    if (latestAttempt.getStatus()
                            == AssessmentStatus.SUBMITTED) {
                        throw new IllegalArgumentException(
                                "The latest assessment attempt is waiting for grading"
                        );
                    }
                });

        Integer maximumAttemptNumber =
                assessmentAttemptRepository
                        .findMaximumAttemptNumber(
                                enrollmentId,
                                contentId
                        );

        int nextAttemptNumber =
                (maximumAttemptNumber == null
                        ? 0
                        : maximumAttemptNumber) + 1;

        LocalDateTime currentTime =
                LocalDateTime.now();

        AssessmentAttempt attempt =
                AssessmentAttempt.builder()
                        .enrollment(enrollment)
                        .content(content)
                        .attemptNumber(nextAttemptNumber)
                        .status(
                                AssessmentStatus.IN_PROGRESS
                        )
                        .startedAt(currentTime)
                        .build();

        AssessmentAttempt savedAttempt =
                assessmentAttemptRepository.save(attempt);

        return mapToResponse(savedAttempt);
    }

    @Override
    public AssessmentAttemptResponseDTO submitAssessmentAttempt(
            UUID attemptId
    ) {
        AssessmentAttempt attempt =
                getAttemptOrThrow(attemptId);

        validateEnrollmentAccess(
                attempt.getEnrollment()
        );

        validateAssessmentAvailableForAttempt(
                attempt.getContent()
        );

        if (attempt.getStatus()
                == AssessmentStatus.SUBMITTED) {
            return mapToResponse(attempt);
        }

        if (attempt.getStatus()
                == AssessmentStatus.PASSED
                || attempt.getStatus()
                == AssessmentStatus.FAILED) {
            throw new IllegalArgumentException(
                    "A graded assessment attempt cannot be submitted again"
            );
        }

        if (attempt.getStatus()
                != AssessmentStatus.IN_PROGRESS) {
            throw new IllegalArgumentException(
                    "Only an in-progress assessment attempt can be submitted"
            );
        }

        attempt.setStatus(
                AssessmentStatus.SUBMITTED
        );

        attempt.setSubmittedAt(
                LocalDateTime.now()
        );

        AssessmentAttempt savedAttempt =
                assessmentAttemptRepository.save(attempt);

        return mapToResponse(savedAttempt);
    }

    @Override
    public AssessmentAttemptResponseDTO gradeAssessmentAttempt(
            UUID attemptId,
            AssessmentGradeRequestDTO request
    ) {
        AssessmentAttempt attempt =
                getAttemptOrThrow(attemptId);

        if (attempt.getStatus()
                != AssessmentStatus.SUBMITTED) {
            throw new IllegalArgumentException(
                    "Only a submitted assessment attempt can be graded"
            );
        }

        validateGradeRequest(
                attempt.getContent(),
                request
        );

        BigDecimal scorePercentage =
                request.getScore()
                        .multiply(
                                BigDecimal.valueOf(100)
                        )
                        .divide(
                                request.getMaximumScore(),
                                2,
                                RoundingMode.HALF_UP
                        );

        Integer passingScore =
                attempt.getEnrollment()
                        .getCourse()
                        .getPassingScore();

        if (passingScore == null) {
            throw new IllegalArgumentException(
                    "Course passing score is not configured"
            );
        }

        AssessmentStatus finalStatus =
                scorePercentage.compareTo(
                        BigDecimal.valueOf(passingScore)
                ) >= 0
                        ? AssessmentStatus.PASSED
                        : AssessmentStatus.FAILED;

        LocalDateTime currentTime =
                LocalDateTime.now();

        attempt.setScore(
                request.getScore()
        );

        attempt.setMaximumScore(
                request.getMaximumScore()
        );

        attempt.setScorePercentage(
                scorePercentage
        );

        attempt.setStatus(finalStatus);

        attempt.setGradedByUserId(
                request.getGradedByUserId()
        );

        attempt.setFeedback(
                normalizeOptionalText(
                        request.getFeedback()
                )
        );

        attempt.setGradedAt(currentTime);

        AssessmentAttempt savedAttempt =
                assessmentAttemptRepository.save(attempt);

        boolean enrollmentAlreadyCompleted =
                savedAttempt.getEnrollment().getStatus()
                        == EnrollmentStatus.COMPLETED;

        if (finalStatus == AssessmentStatus.PASSED
                && !enrollmentAlreadyCompleted) {
            markAssessmentContentCompleted(savedAttempt);
        }

        return mapToResponse(savedAttempt);
    }

    @Override
    @Transactional(readOnly = true)
    public AssessmentAttemptResponseDTO
    getAssessmentAttemptById(
            UUID attemptId
    ) {
        return mapToResponse(
                getAttemptOrThrow(attemptId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssessmentAttemptResponseDTO>
    getAttemptsByEnrollment(
            UUID enrollmentId
    ) {
        getEnrollmentOrThrow(enrollmentId);

        return assessmentAttemptRepository
                .findAllByEnrollmentEnrollmentIdOrderByStartedAtDesc(
                        enrollmentId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssessmentAttemptResponseDTO>
    getAttemptsByEnrollmentAndContent(
            UUID enrollmentId,
            UUID contentId
    ) {
        Enrollment enrollment =
                getEnrollmentOrThrow(enrollmentId);

        getAssessmentContentOrThrow(
                contentId,
                enrollment.getCourse().getCourseId()
        );

        return assessmentAttemptRepository
                .findAllByEnrollmentEnrollmentIdAndContentContentIdOrderByAttemptNumberDesc(
                        enrollmentId,
                        contentId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AssessmentAttemptResponseDTO getLatestAttempt(
            UUID enrollmentId,
            UUID contentId
    ) {
        Enrollment enrollment =
                getEnrollmentOrThrow(enrollmentId);

        getAssessmentContentOrThrow(
                contentId,
                enrollment.getCourse().getCourseId()
        );

        AssessmentAttempt latestAttempt =
                assessmentAttemptRepository
                        .findTopByEnrollmentEnrollmentIdAndContentContentIdOrderByAttemptNumberDesc(
                                enrollmentId,
                                contentId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "No assessment attempt found "
                                                + "for enrollment "
                                                + enrollmentId
                                                + " and content "
                                                + contentId
                                )
                        );

        return mapToResponse(latestAttempt);
    }

    @Override
    @Transactional(readOnly = true)
    public AssessmentAttemptResponseDTO getBestPassedAttempt(
            UUID enrollmentId,
            UUID contentId
    ) {
        Enrollment enrollment =
                getEnrollmentOrThrow(enrollmentId);

        getAssessmentContentOrThrow(
                contentId,
                enrollment.getCourse().getCourseId()
        );

        AssessmentAttempt bestAttempt =
                assessmentAttemptRepository
                        .findTopByEnrollmentEnrollmentIdAndContentContentIdAndStatusOrderByScorePercentageDescGradedAtDesc(
                                enrollmentId,
                                contentId,
                                AssessmentStatus.PASSED
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "No passed assessment attempt found "
                                                + "for enrollment "
                                                + enrollmentId
                                                + " and content "
                                                + contentId
                                )
                        );

        return mapToResponse(bestAttempt);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPassedAssessment(
            UUID enrollmentId,
            UUID contentId
    ) {
        Enrollment enrollment =
                getEnrollmentOrThrow(enrollmentId);

        getAssessmentContentOrThrow(
                contentId,
                enrollment.getCourse().getCourseId()
        );

        return assessmentAttemptRepository
                .existsByEnrollmentEnrollmentIdAndContentContentIdAndStatus(
                        enrollmentId,
                        contentId,
                        AssessmentStatus.PASSED
                );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean areAllMandatoryAssessmentsPassed(
            UUID enrollmentId
    ) {
        Enrollment enrollment =
                getEnrollmentOrThrow(enrollmentId);

        List<CourseContent> publishedContents =
                courseContentRepository
                        .findByCourseModuleCourseCourseIdAndCourseModulePublishedTrueAndPublishedTrueOrderByCourseModuleModuleOrderAscContentOrderAsc(
                                enrollment.getCourse().getCourseId()
                        );

        List<CourseContent> mandatoryAssessments =
                publishedContents.stream()
                        .filter(content ->
                                Boolean.TRUE.equals(
                                        content.getMandatory()
                                )
                        )
                        .filter(this::isAssessmentContent)
                        .toList();

        if (mandatoryAssessments.isEmpty()) {
            return true;
        }

        return mandatoryAssessments.stream()
                .allMatch(content ->
                        assessmentAttemptRepository
                                .existsByEnrollmentEnrollmentIdAndContentContentIdAndStatus(
                                        enrollmentId,
                                        content.getContentId(),
                                        AssessmentStatus.PASSED
                                )
                );
    }

    @Override
    @Transactional(readOnly = true)
    public long countAttemptsByEnrollment(
            UUID enrollmentId
    ) {
        getEnrollmentOrThrow(enrollmentId);

        return assessmentAttemptRepository
                .countByEnrollmentEnrollmentId(
                        enrollmentId
                );
    }

    @Override
    @Transactional(readOnly = true)
    public long countAttemptsByEnrollmentAndStatus(
            UUID enrollmentId,
            AssessmentStatus status
    ) {
        getEnrollmentOrThrow(enrollmentId);

        if (status == null) {
            throw new IllegalArgumentException(
                    "Assessment status is required"
            );
        }

        return assessmentAttemptRepository
                .countByEnrollmentEnrollmentIdAndStatus(
                        enrollmentId,
                        status
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

    private AssessmentAttempt getAttemptOrThrow(
            UUID attemptId
    ) {
        return assessmentAttemptRepository
                .findById(attemptId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Assessment attempt not found with ID: "
                                        + attemptId
                        )
                );
    }

    private CourseContent getAssessmentContentOrThrow(
            UUID contentId,
            UUID courseId
    ) {
        CourseContent content =
                courseContentRepository
                        .findByContentIdAndCourseModuleCourseCourseId(
                                contentId,
                                courseId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Assessment content not found with ID: "
                                                + contentId
                                                + " in course: "
                                                + courseId
                                )
                        );

        validateAssessmentType(content);

        return content;
    }

    private void validateEnrollmentAccess(
            Enrollment enrollment
    ) {
        EnrollmentStatus enrollmentStatus =
                enrollment.getStatus();

        boolean statusAllowed =
                enrollmentStatus
                        == EnrollmentStatus.ACTIVE
                        || enrollmentStatus
                        == EnrollmentStatus.COMPLETED;

        if (!statusAllowed) {
            throw new IllegalArgumentException(
                    "Only an active or completed enrollment "
                            + "can access assessments"
            );
        }

        Course course =
                enrollment.getCourse();

        if (course.getStatus()
                != CourseStatus.PUBLISHED) {
            throw new IllegalArgumentException(
                    "The course must be published "
                            + "to access assessments"
            );
        }

        PaymentStatus paymentStatus =
                enrollment.getPaymentStatus();

        boolean paymentAllowed =
                paymentStatus == PaymentStatus.PAID
                        || paymentStatus
                        == PaymentStatus.NOT_REQUIRED;

        if (!paymentAllowed) {
            throw new IllegalArgumentException(
                    "Course payment must be completed "
                            + "before accessing assessments"
            );
        }

        LocalDateTime accessExpiresAt =
                enrollment.getAccessExpiresAt();

        if (accessExpiresAt != null
                && !accessExpiresAt.isAfter(
                LocalDateTime.now()
        )) {
            throw new IllegalArgumentException(
                    "Course access has expired"
            );
        }
    }

    private void validateAssessmentType(
            CourseContent content
    ) {
        if (!isAssessmentContent(content)) {
            throw new IllegalArgumentException(
                    "Content type must be QUIZ or ASSIGNMENT "
                            + "for assessment attempts"
            );
        }
    }

    private boolean isAssessmentContent(
            CourseContent content
    ) {
        return content.getContentType()
                == ContentType.QUIZ
                || content.getContentType()
                == ContentType.ASSIGNMENT;
    }

    private void validateAssessmentAvailableForAttempt(
            CourseContent content
    ) {
        CourseModule courseModule =
                content.getCourseModule();

        Course course =
                courseModule.getCourse();

        if (course.getStatus()
                != CourseStatus.PUBLISHED) {
            throw new IllegalArgumentException(
                    "Course must be published before "
                            + "an assessment can be attempted"
            );
        }

        if (!Boolean.TRUE.equals(
                courseModule.getPublished()
        )) {
            throw new IllegalArgumentException(
                    "Assessment module is not published"
            );
        }

        if (!Boolean.TRUE.equals(
                content.getPublished()
        )) {
            throw new IllegalArgumentException(
                    "Assessment content is not published"
            );
        }

        LocalDateTime currentTime =
                LocalDateTime.now();

        if (content.getAvailableFrom() != null
                && currentTime.isBefore(
                content.getAvailableFrom()
        )) {
            throw new IllegalArgumentException(
                    "Assessment is not available yet"
            );
        }

        if (content.getAvailableUntil() != null
                && currentTime.isAfter(
                content.getAvailableUntil()
        )) {
            throw new IllegalArgumentException(
                    "Assessment availability has expired"
            );
        }
    }

    private void validateGradeRequest(
            CourseContent content,
            AssessmentGradeRequestDTO request
    ) {
        if (request.getScore().compareTo(
                request.getMaximumScore()
        ) > 0) {
            throw new IllegalArgumentException(
                    "Score cannot exceed maximum score"
            );
        }

        if (content.getContentType()
                == ContentType.ASSIGNMENT
                && request.getGradedByUserId() == null) {
            throw new IllegalArgumentException(
                    "Grader user ID is required "
                            + "for assignment grading"
            );
        }
    }

    private void markAssessmentContentCompleted(
            AssessmentAttempt attempt
    ) {
        ContentProgressUpdateRequestDTO progressRequest =
                ContentProgressUpdateRequestDTO.builder()
                        .progressPercentage(100)
                        .lastPositionSeconds(0L)
                        .additionalTimeSpentSeconds(0L)
                        .build();

        contentProgressService.updateContentProgress(
                attempt.getEnrollment().getEnrollmentId(),
                attempt.getContent().getContentId(),
                progressRequest
        );
    }

    private AssessmentAttemptResponseDTO mapToResponse(
            AssessmentAttempt attempt
    ) {
        Enrollment enrollment =
                attempt.getEnrollment();

        CourseContent content =
                attempt.getContent();

        CourseModule courseModule =
                content.getCourseModule();

        Course course =
                courseModule.getCourse();

        return AssessmentAttemptResponseDTO.builder()
                .attemptId(
                        attempt.getAttemptId()
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
                .passingScore(
                        course.getPassingScore()
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
                .attemptNumber(
                        attempt.getAttemptNumber()
                )
                .status(
                        attempt.getStatus()
                )
                .score(
                        attempt.getScore()
                )
                .maximumScore(
                        attempt.getMaximumScore()
                )
                .scorePercentage(
                        attempt.getScorePercentage()
                )
                .gradedByUserId(
                        attempt.getGradedByUserId()
                )
                .feedback(
                        attempt.getFeedback()
                )
                .startedAt(
                        attempt.getStartedAt()
                )
                .submittedAt(
                        attempt.getSubmittedAt()
                )
                .gradedAt(
                        attempt.getGradedAt()
                )
                .createdAt(
                        attempt.getCreatedAt()
                )
                .updatedAt(
                        attempt.getUpdatedAt()
                )
                .build();
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
}