package com.skillspherenexus.learningservice.repository;

import com.skillspherenexus.learningservice.entity.AssessmentAttempt;
import com.skillspherenexus.learningservice.enums.AssessmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssessmentAttemptRepository
        extends JpaRepository<AssessmentAttempt, UUID> {

    List<AssessmentAttempt>
    findAllByEnrollmentEnrollmentIdOrderByStartedAtDesc(
            UUID enrollmentId
    );

    List<AssessmentAttempt>
    findAllByEnrollmentEnrollmentIdAndContentContentIdOrderByAttemptNumberDesc(
            UUID enrollmentId,
            UUID contentId
    );

    Optional<AssessmentAttempt>
    findTopByEnrollmentEnrollmentIdAndContentContentIdOrderByAttemptNumberDesc(
            UUID enrollmentId,
            UUID contentId
    );

    Optional<AssessmentAttempt>
    findTopByEnrollmentEnrollmentIdAndContentContentIdAndStatusOrderByScorePercentageDescGradedAtDesc(
            UUID enrollmentId,
            UUID contentId,
            AssessmentStatus status
    );

    boolean existsByEnrollmentEnrollmentIdAndContentContentIdAndStatus(
            UUID enrollmentId,
            UUID contentId,
            AssessmentStatus status
    );

    long countByEnrollmentEnrollmentId(
            UUID enrollmentId
    );

    long countByEnrollmentEnrollmentIdAndStatus(
            UUID enrollmentId,
            AssessmentStatus status
    );

    @Query("""
            SELECT COALESCE(MAX(attempt.attemptNumber), 0)
            FROM AssessmentAttempt attempt
            WHERE attempt.enrollment.enrollmentId = :enrollmentId
              AND attempt.content.contentId = :contentId
            """)
    Integer findMaximumAttemptNumber(
            @Param("enrollmentId") UUID enrollmentId,
            @Param("contentId") UUID contentId
    );
}