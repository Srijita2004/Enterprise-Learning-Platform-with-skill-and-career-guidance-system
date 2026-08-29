package com.skillspherenexus.learningservice.service.impl;

import com.skillspherenexus.learningservice.dto.CertificateIssueRequestDTO;
import com.skillspherenexus.learningservice.dto.CourseCompletionResponseDTO;
import com.skillspherenexus.learningservice.dto.CourseProgressSummaryResponseDTO;
import com.skillspherenexus.learningservice.entity.AppUser;
import com.skillspherenexus.learningservice.entity.Course;
import com.skillspherenexus.learningservice.entity.CourseCompletion;
import com.skillspherenexus.learningservice.entity.Enrollment;
import com.skillspherenexus.learningservice.enums.EnrollmentStatus;
import com.skillspherenexus.learningservice.event.CourseCompletedEvent;
import com.skillspherenexus.learningservice.exception.ResourceNotFoundException;
import com.skillspherenexus.learningservice.kafka.LearningEventProducer;
import com.skillspherenexus.learningservice.repository.AppUserRepository;
import com.skillspherenexus.learningservice.repository.CourseCompletionRepository;
import com.skillspherenexus.learningservice.repository.EnrollmentRepository;
import com.skillspherenexus.learningservice.service.AssessmentAttemptService;
import com.skillspherenexus.learningservice.service.CertificateService;
import com.skillspherenexus.learningservice.service.ContentProgressService;
import com.skillspherenexus.learningservice.service.CourseCompletionService;
import com.skillspherenexus.learningservice.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseCompletionServiceImpl
        implements CourseCompletionService {

    private final CourseCompletionRepository
            courseCompletionRepository;

    private final EnrollmentRepository
            enrollmentRepository;

    private final ContentProgressService
            contentProgressService;

    private final AssessmentAttemptService
            assessmentAttemptService;

    private final EnrollmentService
            enrollmentService;

    private final CertificateService
            certificateService;

    private final AppUserRepository
            appUserRepository;

    private final LearningEventProducer
            learningEventProducer;

    @Override
    public CourseCompletionResponseDTO completeCourse(
            UUID enrollmentId
    ) {
        CourseCompletion existingCompletion =
                courseCompletionRepository
                        .findByEnrollmentEnrollmentId(
                                enrollmentId
                        )
                        .orElse(null);

        if (existingCompletion != null) {
            ensureCertificateExists(existingCompletion);
            return mapToResponse(existingCompletion);
        }

        Enrollment enrollment =
                getEnrollmentOrThrow(enrollmentId);

        if (enrollment.getStatus()
                != EnrollmentStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Only an active enrollment can be completed"
            );
        }

        CourseProgressSummaryResponseDTO summary =
                contentProgressService
                        .getCourseProgressSummary(
                                enrollmentId
                        );

        validatePublishedContentExists(summary);

        validateContentCompletion(summary);

        boolean mandatoryAssessmentsPassed =
                assessmentAttemptService
                        .areAllMandatoryAssessmentsPassed(
                                enrollmentId
                        );

        if (!mandatoryAssessmentsPassed) {
            throw new IllegalArgumentException(
                    "All mandatory assessments must be passed "
                            + "before completing the course"
            );
        }

        enrollmentService.completeEnrollment(
                enrollmentId
        );

        Enrollment completedEnrollment =
                getEnrollmentOrThrow(enrollmentId);

        Course course =
                completedEnrollment.getCourse();

        boolean certificateEligible =
                Boolean.TRUE.equals(
                        course.getCertificateEnabled()
                );

        CourseCompletion completion =
                CourseCompletion.builder()
                        .enrollment(completedEnrollment)
                        .learnerId(
                                completedEnrollment.getLearnerId()
                        )
                        .courseId(
                                course.getCourseId()
                        )
                        .overallProgressPercentage(
                                toPercentage(
                                        summary.getOverallProgressPercentage()
                                )
                        )
                        .mandatoryProgressPercentage(
                                toPercentage(
                                        summary.getMandatoryProgressPercentage()
                                )
                        )
                        .totalPublishedContents(
                                safeLong(
                                        summary.getTotalPublishedContents()
                                )
                        )
                        .completedContents(
                                safeLong(
                                        summary.getCompletedContents()
                                )
                        )
                        .totalMandatoryContents(
                                safeLong(
                                        summary.getTotalMandatoryContents()
                                )
                        )
                        .completedMandatoryContents(
                                safeLong(
                                        summary.getCompletedMandatoryContents()
                                )
                        )
                        .mandatoryAssessmentsPassed(
                                mandatoryAssessmentsPassed
                        )
                        .certificateEligible(
                                certificateEligible
                        )
                        .totalTimeSpentSeconds(
                                safeLong(
                                        summary.getTotalTimeSpentSeconds()
                                )
                        )
                        .completedAt(
                                completedEnrollment.getCompletedAt()
                        )
                        .build();

        CourseCompletion savedCompletion =
                courseCompletionRepository.save(
                        completion
                );

        ensureCertificateExists(savedCompletion);

        learningEventProducer.publishCourseCompleted(
                CourseCompletedEvent.builder()
                        .completionId(savedCompletion.getCompletionId())
                        .enrollmentId(completedEnrollment.getEnrollmentId())
                        .learnerId(savedCompletion.getLearnerId())
                        .courseId(savedCompletion.getCourseId())
                        .courseTitle(course.getTitle())
                        .certificateEligible(savedCompletion.getCertificateEligible())
                        .build()
        );

        return mapToResponse(savedCompletion);
    }

    private void ensureCertificateExists(CourseCompletion completion) {
        if (!Boolean.TRUE.equals(completion.getCertificateEligible())) {
            return;
        }

        String recipientName = appUserRepository
                .findById(completion.getLearnerId())
                .map(AppUser::getFullName)
                .orElse("SkillSphere Learner");

        certificateService.issueCertificate(
                completion.getCompletionId(),
                CertificateIssueRequestDTO.builder()
                        .recipientName(recipientName)
                        .build()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CourseCompletionResponseDTO getCompletionById(
            UUID completionId
    ) {
        CourseCompletion completion =
                courseCompletionRepository
                        .findById(completionId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Course completion not found "
                                                + "with ID: "
                                                + completionId
                                )
                        );

        return mapToResponse(completion);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseCompletionResponseDTO
    getCompletionByEnrollment(
            UUID enrollmentId
    ) {
        CourseCompletion completion =
                courseCompletionRepository
                        .findByEnrollmentEnrollmentId(
                                enrollmentId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Course completion not found "
                                                + "for enrollment: "
                                                + enrollmentId
                                )
                        );

        return mapToResponse(completion);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseCompletionResponseDTO
    getCompletionByLearnerAndCourse(
            UUID learnerId,
            UUID courseId
    ) {
        CourseCompletion completion =
                courseCompletionRepository
                        .findByLearnerIdAndCourseId(
                                learnerId,
                                courseId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Course completion not found "
                                                + "for learner "
                                                + learnerId
                                                + " and course "
                                                + courseId
                                )
                        );

        return mapToResponse(completion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseCompletionResponseDTO>
    getCompletionsByLearner(
            UUID learnerId
    ) {
        return courseCompletionRepository
                .findAllByLearnerIdOrderByCompletedAtDesc(
                        learnerId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseCompletionResponseDTO>
    getCompletionsByCourse(
            UUID courseId
    ) {
        return courseCompletionRepository
                .findAllByCourseIdOrderByCompletedAtDesc(
                        courseId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCourseCompleted(
            UUID enrollmentId
    ) {
        getEnrollmentOrThrow(enrollmentId);

        return courseCompletionRepository
                .existsByEnrollmentEnrollmentId(
                        enrollmentId
                );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCertificateEligible(
            UUID enrollmentId
    ) {
        getEnrollmentOrThrow(enrollmentId);

        return courseCompletionRepository
                .findByEnrollmentEnrollmentId(
                        enrollmentId
                )
                .map(CourseCompletion::getCertificateEligible)
                .map(Boolean.TRUE::equals)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public long countCompletionsByLearner(
            UUID learnerId
    ) {
        return courseCompletionRepository
                .countByLearnerId(learnerId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countCompletionsByCourse(
            UUID courseId
    ) {
        return courseCompletionRepository
                .countByCourseId(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countCertificateEligibleCompletionsByCourse(
            UUID courseId
    ) {
        return courseCompletionRepository
                .countByCourseIdAndCertificateEligibleTrue(
                        courseId
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

    private void validatePublishedContentExists(
            CourseProgressSummaryResponseDTO summary
    ) {
        if (safeLong(
                summary.getTotalPublishedContents()
        ) <= 0L) {
            throw new IllegalArgumentException(
                    "Course must contain at least one "
                            + "published content before completion"
            );
        }
    }

    private void validateContentCompletion(
            CourseProgressSummaryResponseDTO summary
    ) {
        long totalMandatoryContents =
                safeLong(
                        summary.getTotalMandatoryContents()
                );

        long completedMandatoryContents =
                safeLong(
                        summary.getCompletedMandatoryContents()
                );

        long totalPublishedContents =
                safeLong(
                        summary.getTotalPublishedContents()
                );

        long completedContents =
                safeLong(
                        summary.getCompletedContents()
                );

        if (totalMandatoryContents > 0L) {
            if (completedMandatoryContents
                    != totalMandatoryContents) {
                throw new IllegalArgumentException(
                        "All mandatory contents must be completed "
                                + "before completing the course"
                );
            }

            return;
        }

        if (completedContents
                != totalPublishedContents) {
            throw new IllegalArgumentException(
                    "All published contents must be completed "
                            + "because the course has no "
                            + "mandatory contents"
            );
        }
    }

    private BigDecimal toPercentage(
            Double percentage
    ) {
        if (percentage == null) {
            return BigDecimal.ZERO
                    .setScale(
                            2,
                            RoundingMode.HALF_UP
                    );
        }

        return BigDecimal
                .valueOf(percentage)
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private long safeLong(
            Long value
    ) {
        return value == null
                ? 0L
                : value;
    }

    private CourseCompletionResponseDTO mapToResponse(
            CourseCompletion completion
    ) {
        Enrollment enrollment =
                completion.getEnrollment();

        Course course =
                enrollment.getCourse();

        return CourseCompletionResponseDTO.builder()
                .completionId(
                        completion.getCompletionId()
                )
                .enrollmentId(
                        enrollment.getEnrollmentId()
                )
                .learnerId(
                        completion.getLearnerId()
                )
                .courseId(
                        completion.getCourseId()
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
                .overallProgressPercentage(
                        completion
                                .getOverallProgressPercentage()
                )
                .mandatoryProgressPercentage(
                        completion
                                .getMandatoryProgressPercentage()
                )
                .totalPublishedContents(
                        completion.getTotalPublishedContents()
                )
                .completedContents(
                        completion.getCompletedContents()
                )
                .totalMandatoryContents(
                        completion.getTotalMandatoryContents()
                )
                .completedMandatoryContents(
                        completion
                                .getCompletedMandatoryContents()
                )
                .mandatoryAssessmentsPassed(
                        completion
                                .getMandatoryAssessmentsPassed()
                )
                .certificateEnabled(
                        course.getCertificateEnabled()
                )
                .certificateEligible(
                        completion.getCertificateEligible()
                )
                .totalTimeSpentSeconds(
                        completion.getTotalTimeSpentSeconds()
                )
                .enrolledAt(
                        enrollment.getEnrolledAt()
                )
                .completedAt(
                        completion.getCompletedAt()
                )
                .createdAt(
                        completion.getCreatedAt()
                )
                .updatedAt(
                        completion.getUpdatedAt()
                )
                .build();
    }
}