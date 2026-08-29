package com.skillspherenexus.learningservice.service.impl;

import com.skillspherenexus.learningservice.dto.EnrollmentCancellationRequestDTO;
import com.skillspherenexus.learningservice.dto.EnrollmentCreateRequestDTO;
import com.skillspherenexus.learningservice.dto.EnrollmentResponseDTO;
import com.skillspherenexus.learningservice.dto.PaymentRejectionRequestDTO;
import com.skillspherenexus.learningservice.dto.PaymentSubmissionRequestDTO;
import com.skillspherenexus.learningservice.entity.AppUser;
import com.skillspherenexus.learningservice.entity.Course;
import com.skillspherenexus.learningservice.entity.Enrollment;
import com.skillspherenexus.learningservice.enums.CoursePricingType;
import com.skillspherenexus.learningservice.enums.CourseStatus;
import com.skillspherenexus.learningservice.enums.EnrollmentSource;
import com.skillspherenexus.learningservice.enums.EnrollmentStatus;
import com.skillspherenexus.learningservice.enums.PaymentStatus;
import com.skillspherenexus.learningservice.enums.UserRole;
import com.skillspherenexus.learningservice.exception.DuplicateResourceException;
import com.skillspherenexus.learningservice.exception.ResourceNotFoundException;
import com.skillspherenexus.learningservice.repository.AppUserRepository;
import com.skillspherenexus.learningservice.repository.CourseRepository;
import com.skillspherenexus.learningservice.repository.EnrollmentRepository;
import com.skillspherenexus.learningservice.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final AppUserRepository appUserRepository;

    @Override
    public EnrollmentResponseDTO createEnrollment(
            UUID courseId,
            EnrollmentCreateRequestDTO request
    ) {
        validateEnrollmentRequest(request);
        Course course = getCourseOrThrow(courseId);
        validateCourseForEnrollment(course);
        validateLearner(request.getLearnerId());
        validateAssigner(request);

        Enrollment existing = enrollmentRepository
                .findByLearnerIdAndCourseCourseId(request.getLearnerId(), courseId)
                .orElse(null);

        if (existing != null) {
            return reactivateExistingEnrollment(existing, course, request);
        }

        validateCourseCapacity(course);
        LocalDateTime now = LocalDateTime.now();
        PaymentStatus paymentStatus = determineInitialPaymentStatus(
                course,
                request.getEnrollmentSource()
        );
        EnrollmentStatus status = determineInitialEnrollmentStatus(paymentStatus);
        String paymentReference = normalizeOptionalText(request.getPaymentReference());

        Enrollment enrollment = Enrollment.builder()
                .learnerId(request.getLearnerId())
                .course(course)
                .status(status)
                .paymentStatus(paymentStatus)
                .paymentReference(paymentReference)
                .paymentSubmittedAt(
                        paymentStatus == PaymentStatus.PENDING && paymentReference != null
                                ? now
                                : null
                )
                .enrollmentSource(request.getEnrollmentSource())
                .assignedByUserId(resolveAssignedByUserId(request))
                .priceAtEnrollment(resolveCoursePrice(course))
                .currencyCode(resolveCurrencyCode(course))
                .enrolledAt(now)
                .activatedAt(status == EnrollmentStatus.ACTIVE ? now : null)
                .accessExpiresAt(request.getAccessExpiresAt())
                .build();

        return mapToResponse(enrollmentRepository.save(enrollment));
    }

    @Override
    @Transactional(readOnly = true)
    public EnrollmentResponseDTO getEnrollmentById(UUID enrollmentId) {
        return mapToResponse(getEnrollmentOrThrow(enrollmentId));
    }

    @Override
    @Transactional(readOnly = true)
    public EnrollmentResponseDTO getEnrollmentByLearnerAndCourse(
            UUID learnerId,
            UUID courseId
    ) {
        Enrollment enrollment = enrollmentRepository
                .findByLearnerIdAndCourseCourseId(learnerId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Enrollment not found for learner " + learnerId
                                + " and course " + courseId
                ));
        return mapToResponse(enrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponseDTO> getAllEnrollments() {
        return enrollmentRepository.findAllByOrderByEnrolledAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponseDTO> getEnrollmentsByLearner(UUID learnerId) {
        return enrollmentRepository.findAllByLearnerIdOrderByEnrolledAtDesc(learnerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponseDTO> getEnrollmentsByCourse(UUID courseId) {
        getCourseOrThrow(courseId);
        return enrollmentRepository.findAllByCourseCourseIdOrderByEnrolledAtDesc(courseId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponseDTO> getEnrollmentsByStatus(EnrollmentStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Enrollment status is required");
        }
        return enrollmentRepository.findAllByStatusOrderByEnrolledAtDesc(status)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponseDTO> getEnrollmentsByPaymentStatus(PaymentStatus paymentStatus) {
        if (paymentStatus == null) {
            throw new IllegalArgumentException("Payment status is required");
        }
        return enrollmentRepository.findAllByPaymentStatusOrderByEnrolledAtDesc(paymentStatus)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponseDTO> getLearnerEnrollmentsByStatus(
            UUID learnerId,
            EnrollmentStatus status
    ) {
        if (status == null) {
            throw new IllegalArgumentException("Enrollment status is required");
        }
        return enrollmentRepository
                .findAllByLearnerIdAndStatusOrderByEnrolledAtDesc(learnerId, status)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public EnrollmentResponseDTO submitPayment(
            UUID enrollmentId,
            PaymentSubmissionRequestDTO request
    ) {
        Enrollment enrollment = getEnrollmentOrThrow(enrollmentId);
        validatePaidEnrollmentCanChangePayment(enrollment);

        String paymentReference = request == null
                ? null
                : normalizeOptionalText(request.getPaymentReference());

        if (paymentReference == null) {
            throw new IllegalArgumentException("Payment reference is required");
        }

        enrollment.setPaymentReference(paymentReference);
        enrollment.setPaymentSubmittedAt(LocalDateTime.now());
        enrollment.setPaymentStatus(PaymentStatus.PENDING);
        enrollment.setPaymentVerifiedAt(null);
        enrollment.setPaymentVerifiedByUserId(null);
        enrollment.setPaymentRejectionReason(null);
        enrollment.setStatus(EnrollmentStatus.PENDING);
        enrollment.setActivatedAt(null);

        return mapToResponse(enrollmentRepository.save(enrollment));
    }

    @Override
    public EnrollmentResponseDTO approvePayment(
            UUID enrollmentId,
            UUID verifiedByUserId
    ) {
        Enrollment enrollment = getEnrollmentOrThrow(enrollmentId);
        validatePaidEnrollmentCanChangePayment(enrollment);
        validateManager(verifiedByUserId, false);

        if (enrollment.getPaymentReference() == null
                || enrollment.getPaymentReference().isBlank()) {
            throw new IllegalArgumentException(
                    "A payment reference must be submitted before approval"
            );
        }

        if (enrollment.getPaymentStatus() == PaymentStatus.PAID
                && enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
            return mapToResponse(enrollment);
        }

        LocalDateTime now = LocalDateTime.now();
        enrollment.setPaymentStatus(PaymentStatus.PAID);
        enrollment.setPaymentVerifiedAt(now);
        enrollment.setPaymentVerifiedByUserId(verifiedByUserId);
        enrollment.setPaymentRejectionReason(null);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setActivatedAt(
                enrollment.getActivatedAt() == null ? now : enrollment.getActivatedAt()
        );
        enrollment.setCancelledAt(null);
        enrollment.setCancellationReason(null);

        return mapToResponse(enrollmentRepository.save(enrollment));
    }

    @Override
    public EnrollmentResponseDTO rejectPayment(
            UUID enrollmentId,
            UUID verifiedByUserId,
            PaymentRejectionRequestDTO request
    ) {
        Enrollment enrollment = getEnrollmentOrThrow(enrollmentId);
        validatePaidEnrollmentCanChangePayment(enrollment);
        validateManager(verifiedByUserId, false);

        String reason = request == null ? null : normalizeOptionalText(request.getReason());
        if (reason == null) {
            throw new IllegalArgumentException("Payment rejection reason is required");
        }

        enrollment.setPaymentStatus(PaymentStatus.REJECTED);
        enrollment.setPaymentVerifiedAt(LocalDateTime.now());
        enrollment.setPaymentVerifiedByUserId(verifiedByUserId);
        enrollment.setPaymentRejectionReason(reason);
        enrollment.setStatus(EnrollmentStatus.PENDING);
        enrollment.setActivatedAt(null);

        return mapToResponse(enrollmentRepository.save(enrollment));
    }

    @Override
    public EnrollmentResponseDTO confirmPayment(UUID enrollmentId) {
        return approvePayment(enrollmentId, null);
    }

    @Override
    public EnrollmentResponseDTO cancelEnrollment(
            UUID enrollmentId,
            EnrollmentCancellationRequestDTO request
    ) {
        Enrollment enrollment = getEnrollmentOrThrow(enrollmentId);

        if (enrollment.getStatus() == EnrollmentStatus.CANCELLED) {
            return mapToResponse(enrollment);
        }
        if (enrollment.getStatus() == EnrollmentStatus.COMPLETED) {
            throw new IllegalArgumentException("A completed enrollment cannot be cancelled");
        }
        if (enrollment.getStatus() == EnrollmentStatus.EXPIRED) {
            throw new IllegalArgumentException("An expired enrollment cannot be cancelled");
        }

        String reason = request == null ? null : normalizeOptionalText(request.getCancellationReason());
        if (reason == null) {
            throw new IllegalArgumentException("Cancellation reason is required");
        }

        enrollment.setStatus(EnrollmentStatus.CANCELLED);
        enrollment.setCancelledAt(LocalDateTime.now());
        enrollment.setCancellationReason(reason);
        return mapToResponse(enrollmentRepository.save(enrollment));
    }

    @Override
    public EnrollmentResponseDTO completeEnrollment(UUID enrollmentId) {
        Enrollment enrollment = getEnrollmentOrThrow(enrollmentId);

        if (enrollment.getStatus() == EnrollmentStatus.COMPLETED) {
            return mapToResponse(enrollment);
        }
        if (!isAccessAllowed(enrollment)) {
            throw new IllegalArgumentException(
                    "Only an active enrollment with valid payment and course access can be completed"
            );
        }

        enrollment.setStatus(EnrollmentStatus.COMPLETED);
        enrollment.setCompletedAt(LocalDateTime.now());
        return mapToResponse(enrollmentRepository.save(enrollment));
    }

    @Override
    public EnrollmentResponseDTO expireEnrollment(UUID enrollmentId) {
        Enrollment enrollment = getEnrollmentOrThrow(enrollmentId);
        if (enrollment.getStatus() == EnrollmentStatus.COMPLETED
                || enrollment.getStatus() == EnrollmentStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Completed or cancelled enrollments cannot be expired"
            );
        }
        if (enrollment.getStatus() == EnrollmentStatus.EXPIRED) {
            return mapToResponse(enrollment);
        }
        enrollment.setStatus(EnrollmentStatus.EXPIRED);
        return mapToResponse(enrollmentRepository.save(enrollment));
    }

    @Override
    public EnrollmentResponseDTO recordCourseAccess(UUID enrollmentId) {
        Enrollment enrollment = getEnrollmentOrThrow(enrollmentId);
        if (!isAccessAllowed(enrollment)) {
            throw new IllegalArgumentException("Learner does not have valid access to this course");
        }
        enrollment.setLastAccessedAt(LocalDateTime.now());
        return mapToResponse(enrollmentRepository.save(enrollment));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveCourseAccess(UUID learnerId, UUID courseId) {
        return enrollmentRepository.findByLearnerIdAndCourseCourseId(learnerId, courseId)
                .map(this::isAccessAllowed)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public long countEnrollmentsByCourse(UUID courseId) {
        getCourseOrThrow(courseId);
        return enrollmentRepository.countByCourseCourseId(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countEnrollmentsByCourseAndStatus(
            UUID courseId,
            EnrollmentStatus status
    ) {
        getCourseOrThrow(courseId);
        if (status == null) {
            throw new IllegalArgumentException("Enrollment status is required");
        }
        return enrollmentRepository.countByCourseCourseIdAndStatus(courseId, status);
    }

    private EnrollmentResponseDTO reactivateExistingEnrollment(
            Enrollment enrollment,
            Course course,
            EnrollmentCreateRequestDTO request
    ) {
        if (enrollment.getStatus() != EnrollmentStatus.CANCELLED
                && enrollment.getStatus() != EnrollmentStatus.EXPIRED) {
            throw new DuplicateResourceException("Learner is already enrolled in this course");
        }

        validateCourseCapacity(course);
        LocalDateTime now = LocalDateTime.now();
        PaymentStatus paymentStatus = determineInitialPaymentStatus(
                course,
                request.getEnrollmentSource()
        );
        EnrollmentStatus status = determineInitialEnrollmentStatus(paymentStatus);
        String paymentReference = normalizeOptionalText(request.getPaymentReference());

        enrollment.setStatus(status);
        enrollment.setPaymentStatus(paymentStatus);
        enrollment.setPaymentReference(paymentReference);
        enrollment.setPaymentSubmittedAt(
                paymentStatus == PaymentStatus.PENDING && paymentReference != null ? now : null
        );
        enrollment.setPaymentVerifiedAt(null);
        enrollment.setPaymentVerifiedByUserId(null);
        enrollment.setPaymentRejectionReason(null);
        enrollment.setEnrollmentSource(request.getEnrollmentSource());
        enrollment.setAssignedByUserId(resolveAssignedByUserId(request));
        enrollment.setPriceAtEnrollment(resolveCoursePrice(course));
        enrollment.setCurrencyCode(resolveCurrencyCode(course));
        enrollment.setEnrolledAt(now);
        enrollment.setActivatedAt(status == EnrollmentStatus.ACTIVE ? now : null);
        enrollment.setCompletedAt(null);
        enrollment.setCancelledAt(null);
        enrollment.setCancellationReason(null);
        enrollment.setAccessExpiresAt(request.getAccessExpiresAt());
        enrollment.setLastAccessedAt(null);

        return mapToResponse(enrollmentRepository.save(enrollment));
    }

    private void validateEnrollmentRequest(EnrollmentCreateRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Enrollment request is required");
        }
        if (request.getLearnerId() == null) {
            throw new IllegalArgumentException("Learner ID is required");
        }
        if (request.getEnrollmentSource() == null) {
            throw new IllegalArgumentException("Enrollment source is required");
        }
        if (request.getAccessExpiresAt() != null
                && request.getAccessExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Access expiry time cannot be in the past");
        }
    }

    private void validateLearner(UUID learnerId) {
        AppUser learner = appUserRepository.findById(learnerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Learner account not found with ID: " + learnerId
                ));
        if (learner.getRole() != UserRole.LEARNER) {
            throw new IllegalArgumentException("Enrollment learner ID must belong to a learner account");
        }
    }

    private void validateAssigner(EnrollmentCreateRequestDTO request) {
        EnrollmentSource source = request.getEnrollmentSource();
        if (source == EnrollmentSource.SELF_ENROLLED) {
            if (request.getAssignedByUserId() != null) {
                throw new IllegalArgumentException(
                        "Assigned-by user ID must not be provided for self enrollment"
                );
            }
            return;
        }
        if (request.getAssignedByUserId() == null) {
            throw new IllegalArgumentException("Assigned-by user ID is required for " + source);
        }
        validateManager(request.getAssignedByUserId(), true);
    }

    private void validateManager(UUID userId, boolean required) {
        if (userId == null) {
            if (required) {
                throw new IllegalArgumentException("Manager user ID is required");
            }
            return;
        }
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Manager account not found with ID: " + userId
                ));
        if (user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.HR) {
            throw new IllegalArgumentException("Only Admin or HR can perform this action");
        }
    }

    private void validatePaidEnrollmentCanChangePayment(Enrollment enrollment) {
        if (enrollment.getCourse().getPricingType() != CoursePricingType.PAID) {
            throw new IllegalArgumentException("Payment verification is not applicable to a free course");
        }
        if (enrollment.getStatus() == EnrollmentStatus.CANCELLED
                || enrollment.getStatus() == EnrollmentStatus.EXPIRED
                || enrollment.getStatus() == EnrollmentStatus.COMPLETED) {
            throw new IllegalArgumentException(
                    "Payment cannot be changed for an enrollment with status " + enrollment.getStatus()
            );
        }
    }

    private void validateCourseForEnrollment(Course course) {
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new IllegalArgumentException("Learners can enroll only in a published course");
        }
    }

    private void validateCourseCapacity(Course course) {
        Integer maxCapacity = course.getMaxCapacity();
        if (maxCapacity == null || maxCapacity <= 0) {
            return;
        }
        long occupiedSeats = enrollmentRepository
                .findAllByCourseCourseIdOrderByEnrolledAtDesc(course.getCourseId())
                .stream()
                .filter(enrollment -> enrollment.getStatus() == EnrollmentStatus.PENDING
                        || enrollment.getStatus() == EnrollmentStatus.ACTIVE)
                .count();
        if (occupiedSeats >= maxCapacity) {
            throw new IllegalArgumentException("Course enrollment capacity has been reached");
        }
    }

    private PaymentStatus determineInitialPaymentStatus(
            Course course,
            EnrollmentSource source
    ) {
        if (course.getPricingType() == CoursePricingType.FREE) {
            return PaymentStatus.NOT_REQUIRED;
        }
        if (source != EnrollmentSource.SELF_ENROLLED) {
            return PaymentStatus.NOT_REQUIRED;
        }
        return PaymentStatus.PENDING;
    }

    private EnrollmentStatus determineInitialEnrollmentStatus(PaymentStatus paymentStatus) {
        return paymentStatus == PaymentStatus.NOT_REQUIRED
                ? EnrollmentStatus.ACTIVE
                : EnrollmentStatus.PENDING;
    }

    private UUID resolveAssignedByUserId(EnrollmentCreateRequestDTO request) {
        return request.getEnrollmentSource() == EnrollmentSource.SELF_ENROLLED
                ? null
                : request.getAssignedByUserId();
    }

    private BigDecimal resolveCoursePrice(Course course) {
        return course.getPrice() == null ? BigDecimal.ZERO : course.getPrice();
    }

    private String resolveCurrencyCode(Course course) {
        return course.getCurrencyCode() == null || course.getCurrencyCode().isBlank()
                ? "INR"
                : course.getCurrencyCode().trim().toUpperCase();
    }

    private boolean isAccessAllowed(Enrollment enrollment) {
        boolean statusValid = enrollment.getStatus() == EnrollmentStatus.ACTIVE
                || enrollment.getStatus() == EnrollmentStatus.COMPLETED;
        if (!statusValid || enrollment.getCourse().getStatus() != CourseStatus.PUBLISHED) {
            return false;
        }
        boolean paymentValid = enrollment.getPaymentStatus() == PaymentStatus.PAID
                || enrollment.getPaymentStatus() == PaymentStatus.NOT_REQUIRED;
        if (!paymentValid) {
            return false;
        }
        return enrollment.getAccessExpiresAt() == null
                || enrollment.getAccessExpiresAt().isAfter(LocalDateTime.now());
    }

    private Course getCourseOrThrow(UUID courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course not found with ID: " + courseId
                ));
    }

    private Enrollment getEnrollmentOrThrow(UUID enrollmentId) {
        return enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Enrollment not found with ID: " + enrollmentId
                ));
    }

    private String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String resolveLearnerName(UUID learnerId) {
        return appUserRepository.findById(learnerId)
                .map(AppUser::getFullName)
                .orElse("Learner " + learnerId.toString().substring(0, 8));
    }

    private EnrollmentResponseDTO mapToResponse(Enrollment enrollment) {
        Course course = enrollment.getCourse();
        return EnrollmentResponseDTO.builder()
                .enrollmentId(enrollment.getEnrollmentId())
                .learnerId(enrollment.getLearnerId())
                .learnerName(resolveLearnerName(enrollment.getLearnerId()))
                .courseId(course.getCourseId())
                .courseCode(course.getCourseCode())
                .courseTitle(course.getTitle())
                .courseType(course.getCourseType())
                .courseStatus(course.getStatus())
                .coursePricingType(course.getPricingType())
                .status(enrollment.getStatus())
                .paymentStatus(enrollment.getPaymentStatus())
                .paymentReference(enrollment.getPaymentReference())
                .paymentSubmittedAt(enrollment.getPaymentSubmittedAt())
                .paymentVerifiedAt(enrollment.getPaymentVerifiedAt())
                .paymentVerifiedByUserId(enrollment.getPaymentVerifiedByUserId())
                .paymentRejectionReason(enrollment.getPaymentRejectionReason())
                .enrollmentSource(enrollment.getEnrollmentSource())
                .assignedByUserId(enrollment.getAssignedByUserId())
                .priceAtEnrollment(enrollment.getPriceAtEnrollment())
                .currencyCode(enrollment.getCurrencyCode())
                .accessAllowed(isAccessAllowed(enrollment))
                .enrolledAt(enrollment.getEnrolledAt())
                .activatedAt(enrollment.getActivatedAt())
                .completedAt(enrollment.getCompletedAt())
                .cancelledAt(enrollment.getCancelledAt())
                .accessExpiresAt(enrollment.getAccessExpiresAt())
                .lastAccessedAt(enrollment.getLastAccessedAt())
                .cancellationReason(enrollment.getCancellationReason())
                .createdAt(enrollment.getCreatedAt())
                .updatedAt(enrollment.getUpdatedAt())
                .build();
    }
}
