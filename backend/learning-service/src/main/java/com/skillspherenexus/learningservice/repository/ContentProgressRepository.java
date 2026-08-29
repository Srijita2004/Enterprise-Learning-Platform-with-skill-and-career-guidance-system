package com.skillspherenexus.learningservice.repository;

import com.skillspherenexus.learningservice.entity.ContentProgress;
import com.skillspherenexus.learningservice.enums.ContentProgressStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContentProgressRepository
        extends JpaRepository<ContentProgress, UUID> {

    Optional<ContentProgress>
    findByEnrollmentEnrollmentIdAndContentContentId(
            UUID enrollmentId,
            UUID contentId
    );

    boolean existsByEnrollmentEnrollmentIdAndContentContentId(
            UUID enrollmentId,
            UUID contentId
    );

    List<ContentProgress>
    findAllByEnrollmentEnrollmentIdOrderByContentCourseModuleModuleOrderAscContentContentOrderAsc(
            UUID enrollmentId
    );

    List<ContentProgress>
    findAllByEnrollmentEnrollmentIdAndStatusOrderByContentCourseModuleModuleOrderAscContentContentOrderAsc(
            UUID enrollmentId,
            ContentProgressStatus status
    );

    long countByEnrollmentEnrollmentId(
            UUID enrollmentId
    );

    long countByEnrollmentEnrollmentIdAndStatus(
            UUID enrollmentId,
            ContentProgressStatus status
    );

    long countByEnrollmentEnrollmentIdAndContentMandatoryTrue(
            UUID enrollmentId
    );

    long countByEnrollmentEnrollmentIdAndContentMandatoryTrueAndStatus(
            UUID enrollmentId,
            ContentProgressStatus status
    );

    @Query("""
            SELECT COALESCE(SUM(progress.timeSpentSeconds), 0)
            FROM ContentProgress progress
            WHERE progress.enrollment.enrollmentId = :enrollmentId
            """)
    Long calculateTotalTimeSpentSeconds(
            @Param("enrollmentId") UUID enrollmentId
    );

    @Query("""
            SELECT COALESCE(AVG(progress.progressPercentage), 0)
            FROM ContentProgress progress
            WHERE progress.enrollment.enrollmentId = :enrollmentId
            """)
    Double calculateAverageProgressPercentage(
            @Param("enrollmentId") UUID enrollmentId
    );
}