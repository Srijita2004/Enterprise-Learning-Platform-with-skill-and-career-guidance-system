package com.skillspherenexus.learningservice.repository;

import com.skillspherenexus.learningservice.entity.CourseContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseContentRepository
        extends JpaRepository<CourseContent, UUID> {

    List<CourseContent>
    findByCourseModuleModuleIdOrderByContentOrderAsc(
            UUID moduleId
    );

    List<CourseContent>
    findByCourseModuleModuleIdAndPublishedTrueOrderByContentOrderAsc(
            UUID moduleId
    );

    Optional<CourseContent>
    findByContentIdAndCourseModuleModuleId(
            UUID contentId,
            UUID moduleId
    );

    Optional<CourseContent>
    findByContentIdAndCourseModuleCourseCourseId(
            UUID contentId,
            UUID courseId
    );

    List<CourseContent>
    findByCourseModuleCourseCourseIdAndCourseModulePublishedTrueAndPublishedTrueOrderByCourseModuleModuleOrderAscContentOrderAsc(
            UUID courseId
    );

    boolean existsByCourseModuleModuleIdAndContentOrder(
            UUID moduleId,
            Integer contentOrder
    );

    boolean existsByCourseModuleModuleIdAndContentOrderAndContentIdNot(
            UUID moduleId,
            Integer contentOrder,
            UUID contentId
    );

    long countByCourseModuleModuleId(
            UUID moduleId
    );

    long countByCourseModuleModuleIdAndPublishedTrue(
            UUID moduleId
    );

    long countByCourseModuleCourseCourseIdAndCourseModulePublishedTrueAndPublishedTrue(
            UUID courseId
    );

    long countByCourseModuleCourseCourseIdAndCourseModulePublishedTrueAndPublishedTrueAndMandatoryTrue(
            UUID courseId
    );

    @Query("""
            SELECT COALESCE(SUM(content.durationMinutes), 0)
            FROM CourseContent content
            WHERE content.courseModule.moduleId = :moduleId
            """)
    Long calculateTotalDurationByModuleId(
            @Param("moduleId") UUID moduleId
    );

    @Query("""
            SELECT COALESCE(SUM(content.durationMinutes), 0)
            FROM CourseContent content
            WHERE content.courseModule.course.courseId = :courseId
              AND content.courseModule.published = true
              AND content.published = true
            """)
    Long calculateTotalPublishedDurationByCourseId(
            @Param("courseId") UUID courseId
    );
}