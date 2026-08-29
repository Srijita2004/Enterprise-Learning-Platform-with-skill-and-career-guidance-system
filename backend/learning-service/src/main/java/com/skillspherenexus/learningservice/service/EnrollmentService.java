package com.skillspherenexus.learningservice.service;

import com.skillspherenexus.learningservice.dto.EnrollmentCancellationRequestDTO;
import com.skillspherenexus.learningservice.dto.EnrollmentCreateRequestDTO;
import com.skillspherenexus.learningservice.dto.EnrollmentResponseDTO;
import com.skillspherenexus.learningservice.dto.PaymentRejectionRequestDTO;
import com.skillspherenexus.learningservice.dto.PaymentSubmissionRequestDTO;
import com.skillspherenexus.learningservice.enums.EnrollmentStatus;
import com.skillspherenexus.learningservice.enums.PaymentStatus;

import java.util.List;
import java.util.UUID;

public interface EnrollmentService {

    EnrollmentResponseDTO createEnrollment(UUID courseId, EnrollmentCreateRequestDTO request);

    EnrollmentResponseDTO getEnrollmentById(UUID enrollmentId);

    EnrollmentResponseDTO getEnrollmentByLearnerAndCourse(UUID learnerId, UUID courseId);

    List<EnrollmentResponseDTO> getAllEnrollments();

    List<EnrollmentResponseDTO> getEnrollmentsByLearner(UUID learnerId);

    List<EnrollmentResponseDTO> getEnrollmentsByCourse(UUID courseId);

    List<EnrollmentResponseDTO> getEnrollmentsByStatus(EnrollmentStatus status);

    List<EnrollmentResponseDTO> getEnrollmentsByPaymentStatus(PaymentStatus paymentStatus);

    List<EnrollmentResponseDTO> getLearnerEnrollmentsByStatus(UUID learnerId, EnrollmentStatus status);

    EnrollmentResponseDTO submitPayment(UUID enrollmentId, PaymentSubmissionRequestDTO request);

    EnrollmentResponseDTO approvePayment(UUID enrollmentId, UUID verifiedByUserId);

    EnrollmentResponseDTO rejectPayment(
            UUID enrollmentId,
            UUID verifiedByUserId,
            PaymentRejectionRequestDTO request
    );

    EnrollmentResponseDTO confirmPayment(UUID enrollmentId);

    EnrollmentResponseDTO cancelEnrollment(
            UUID enrollmentId,
            EnrollmentCancellationRequestDTO request
    );

    EnrollmentResponseDTO completeEnrollment(UUID enrollmentId);

    EnrollmentResponseDTO expireEnrollment(UUID enrollmentId);

    EnrollmentResponseDTO recordCourseAccess(UUID enrollmentId);

    boolean hasActiveCourseAccess(UUID learnerId, UUID courseId);

    long countEnrollmentsByCourse(UUID courseId);

    long countEnrollmentsByCourseAndStatus(UUID courseId, EnrollmentStatus status);
}
