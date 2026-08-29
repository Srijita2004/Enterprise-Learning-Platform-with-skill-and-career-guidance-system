package com.skillspherenexus.learningservice.repository;

import com.skillspherenexus.learningservice.entity.Enrollment;
import com.skillspherenexus.learningservice.enums.EnrollmentStatus;
import com.skillspherenexus.learningservice.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnrollmentRepository
        extends JpaRepository<Enrollment, UUID> {

    boolean existsByLearnerIdAndCourseCourseId(
            UUID learnerId,
            UUID courseId
    );

    Optional<Enrollment> findByLearnerIdAndCourseCourseId(
            UUID learnerId,
            UUID courseId
    );

    List<Enrollment> findAllByLearnerIdOrderByEnrolledAtDesc(
            UUID learnerId
    );

    List<Enrollment> findAllByCourseCourseIdOrderByEnrolledAtDesc(
            UUID courseId
    );

    List<Enrollment> findAllByStatusOrderByEnrolledAtDesc(
            EnrollmentStatus status
    );

    List<Enrollment> findAllByPaymentStatusOrderByEnrolledAtDesc(
            PaymentStatus paymentStatus
    );

    List<Enrollment> findAllByOrderByEnrolledAtDesc();

    List<Enrollment>
    findAllByLearnerIdAndStatusOrderByEnrolledAtDesc(
            UUID learnerId,
            EnrollmentStatus status
    );

    long countByCourseCourseId(
            UUID courseId
    );

    long countByCourseCourseIdAndStatus(
            UUID courseId,
            EnrollmentStatus status
    );

    long countByStatus(EnrollmentStatus status);

    long countByPaymentStatus(PaymentStatus paymentStatus);
}