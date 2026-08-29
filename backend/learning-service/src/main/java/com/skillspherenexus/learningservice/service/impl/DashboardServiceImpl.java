package com.skillspherenexus.learningservice.service.impl;

import com.skillspherenexus.learningservice.dto.HrDashboardResponseDTO;
import com.skillspherenexus.learningservice.dto.LearnerDashboardResponseDTO;
import com.skillspherenexus.learningservice.entity.Enrollment;
import com.skillspherenexus.learningservice.enums.AssessmentStatus;
import com.skillspherenexus.learningservice.enums.CourseStatus;
import com.skillspherenexus.learningservice.enums.CertificateStatus;
import com.skillspherenexus.learningservice.enums.EnrollmentStatus;
import com.skillspherenexus.learningservice.enums.LearningPathAssignmentStatus;
import com.skillspherenexus.learningservice.enums.LearningPathStatus;
import com.skillspherenexus.learningservice.enums.PaymentStatus;
import com.skillspherenexus.learningservice.enums.UserRole;
import com.skillspherenexus.learningservice.repository.AppUserRepository;
import com.skillspherenexus.learningservice.repository.AssessmentAttemptRepository;
import com.skillspherenexus.learningservice.repository.CertificateRepository;
import com.skillspherenexus.learningservice.repository.ContentProgressRepository;
import com.skillspherenexus.learningservice.repository.CourseRepository;
import com.skillspherenexus.learningservice.repository.EnrollmentRepository;
import com.skillspherenexus.learningservice.repository.LearningPathAssignmentRepository;
import com.skillspherenexus.learningservice.repository.LearningPathRepository;
import com.skillspherenexus.learningservice.service.DashboardService;
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
@Transactional(readOnly = true)
public class DashboardServiceImpl
        implements DashboardService {

    private final EnrollmentRepository enrollmentRepository;

    private final ContentProgressRepository
            contentProgressRepository;

    private final AssessmentAttemptRepository
            assessmentAttemptRepository;

    private final CertificateRepository certificateRepository;

    private final LearningPathAssignmentRepository
            learningPathAssignmentRepository;

    private final AppUserRepository appUserRepository;

    private final CourseRepository courseRepository;

    private final LearningPathRepository learningPathRepository;

    @Override
    public LearnerDashboardResponseDTO getLearnerDashboard(
            UUID learnerId
    ) {
        if (learnerId == null) {
            throw new IllegalArgumentException(
                    "Learner ID is required"
            );
        }

        List<Enrollment> enrollments =
                enrollmentRepository
                        .findAllByLearnerIdOrderByEnrolledAtDesc(
                                learnerId
                        );

        long totalEnrollments =
                enrollments.size();

        long activeEnrollments =
                countEnrollmentsByStatus(
                        enrollments,
                        EnrollmentStatus.ACTIVE
                );

        long completedCourses =
                countEnrollmentsByStatus(
                        enrollments,
                        EnrollmentStatus.COMPLETED
                );

        long pendingEnrollments =
                countEnrollmentsByStatus(
                        enrollments,
                        EnrollmentStatus.PENDING
                );

        BigDecimal averageCourseProgress =
                calculateAverageCourseProgress(
                        enrollments
                );

        long totalAssessmentAttempts =
                calculateTotalAssessmentAttempts(
                        enrollments
                );

        long passedAssessments =
                calculateAssessmentCountByStatus(
                        enrollments,
                        AssessmentStatus.PASSED
                );

        long failedAssessments =
                calculateAssessmentCountByStatus(
                        enrollments,
                        AssessmentStatus.FAILED
                );

        long certificatesEarned =
                certificateRepository
                        .findAllByLearnerIdOrderByIssuedAtDesc(
                                learnerId
                        )
                        .stream()
                        .filter(certificate ->
                                certificate.getStatus()
                                        == CertificateStatus.ISSUED
                        )
                        .count();

        long assignedLearningPaths =
                learningPathAssignmentRepository
                        .countByLearnerId(
                                learnerId
                        );

        long inProgressLearningPaths =
                learningPathAssignmentRepository
                        .countByLearnerIdAndStatus(
                                learnerId,
                                LearningPathAssignmentStatus.IN_PROGRESS
                        );

        long completedLearningPaths =
                learningPathAssignmentRepository
                        .countByLearnerIdAndStatus(
                                learnerId,
                                LearningPathAssignmentStatus.COMPLETED
                        );

        return LearnerDashboardResponseDTO.builder()
                .learnerId(learnerId)
                .totalEnrollments(totalEnrollments)
                .activeEnrollments(activeEnrollments)
                .completedCourses(completedCourses)
                .pendingEnrollments(pendingEnrollments)
                .averageCourseProgress(
                        averageCourseProgress
                )
                .totalAssessmentAttempts(
                        totalAssessmentAttempts
                )
                .passedAssessments(
                        passedAssessments
                )
                .failedAssessments(
                        failedAssessments
                )
                .certificatesEarned(
                        certificatesEarned
                )
                .assignedLearningPaths(
                        assignedLearningPaths
                )
                .inProgressLearningPaths(
                        inProgressLearningPaths
                )
                .completedLearningPaths(
                        completedLearningPaths
                )
                .generatedAt(
                        LocalDateTime.now()
                )
                .build();
    }

    @Override
    public HrDashboardResponseDTO getHrDashboard() {
        return HrDashboardResponseDTO.builder()
                .totalLearners(appUserRepository.countByRole(UserRole.LEARNER))
                .totalCourses(courseRepository.count())
                .publishedCourses(courseRepository.countByStatus(CourseStatus.PUBLISHED))
                .totalEnrollments(enrollmentRepository.count())
                .activeEnrollments(enrollmentRepository.countByStatus(EnrollmentStatus.ACTIVE))
                .completedEnrollments(enrollmentRepository.countByStatus(EnrollmentStatus.COMPLETED))
                .pendingPayments(enrollmentRepository.countByPaymentStatus(PaymentStatus.PENDING))
                .issuedCertificates(certificateRepository.countByStatus(CertificateStatus.ISSUED))
                .totalLearningPaths(learningPathRepository.count())
                .publishedLearningPaths(learningPathRepository.countByStatus(LearningPathStatus.PUBLISHED))
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private long countEnrollmentsByStatus(
            List<Enrollment> enrollments,
            EnrollmentStatus status
    ) {
        return enrollments
                .stream()
                .filter(enrollment ->
                        enrollment.getStatus() == status
                )
                .count();
    }

    private BigDecimal calculateAverageCourseProgress(
            List<Enrollment> enrollments
    ) {
        double totalProgress = 0.0;
        long relevantEnrollmentCount = 0;

        for (Enrollment enrollment : enrollments) {

            if (enrollment.getStatus()
                    == EnrollmentStatus.COMPLETED) {

                totalProgress += 100.0;
                relevantEnrollmentCount++;
                continue;
            }

            if (enrollment.getStatus()
                    != EnrollmentStatus.ACTIVE) {
                continue;
            }

            Double enrollmentProgress =
                    contentProgressRepository
                            .calculateAverageProgressPercentage(
                                    enrollment.getEnrollmentId()
                            );

            totalProgress +=
                    enrollmentProgress == null
                            ? 0.0
                            : enrollmentProgress;

            relevantEnrollmentCount++;
        }

        if (relevantEnrollmentCount == 0) {
            return BigDecimal.ZERO.setScale(
                    2,
                    RoundingMode.HALF_UP
            );
        }

        double averageProgress =
                totalProgress
                        / relevantEnrollmentCount;

        return BigDecimal
                .valueOf(averageProgress)
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private long calculateTotalAssessmentAttempts(
            List<Enrollment> enrollments
    ) {
        long totalAttempts = 0;

        for (Enrollment enrollment : enrollments) {
            totalAttempts +=
                    assessmentAttemptRepository
                            .countByEnrollmentEnrollmentId(
                                    enrollment.getEnrollmentId()
                            );
        }

        return totalAttempts;
    }

    private long calculateAssessmentCountByStatus(
            List<Enrollment> enrollments,
            AssessmentStatus status
    ) {
        long totalCount = 0;

        for (Enrollment enrollment : enrollments) {
            totalCount +=
                    assessmentAttemptRepository
                            .countByEnrollmentEnrollmentIdAndStatus(
                                    enrollment.getEnrollmentId(),
                                    status
                            );
        }

        return totalCount;
    }
}