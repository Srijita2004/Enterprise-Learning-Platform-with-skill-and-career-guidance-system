package com.skillspherenexus.learningservice.repository;

import com.skillspherenexus.learningservice.entity.CourseCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseCompletionRepository
        extends JpaRepository<CourseCompletion, UUID> {

    Optional<CourseCompletion>
    findByEnrollmentEnrollmentId(
            UUID enrollmentId
    );

    boolean existsByEnrollmentEnrollmentId(
            UUID enrollmentId
    );

    Optional<CourseCompletion>
    findByLearnerIdAndCourseId(
            UUID learnerId,
            UUID courseId
    );

    List<CourseCompletion>
    findAllByLearnerIdOrderByCompletedAtDesc(
            UUID learnerId
    );

    List<CourseCompletion>
    findAllByCourseIdOrderByCompletedAtDesc(
            UUID courseId
    );

    long countByLearnerId(
            UUID learnerId
    );

    long countByCourseId(
            UUID courseId
    );

    long countByCourseIdAndCertificateEligibleTrue(
            UUID courseId
    );
}