package com.skillspherenexus.learningservice.repository;

import com.skillspherenexus.learningservice.entity.LearningPathCourse;
import com.skillspherenexus.learningservice.enums.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LearningPathCourseRepository
        extends JpaRepository<LearningPathCourse, UUID> {

    List<LearningPathCourse>
    findAllByLearningPathPathIdOrderByCourseOrderAsc(
            UUID pathId
    );

    Optional<LearningPathCourse>
    findByLearningPathPathIdAndCourseCourseId(
            UUID pathId,
            UUID courseId
    );

    Optional<LearningPathCourse>
    findByLearningPathPathIdAndCourseOrder(
            UUID pathId,
            Integer courseOrder
    );

    boolean existsByLearningPathPathIdAndCourseCourseId(
            UUID pathId,
            UUID courseId
    );

    boolean existsByLearningPathPathIdAndCourseOrder(
            UUID pathId,
            Integer courseOrder
    );

    boolean
    existsByLearningPathPathIdAndCourseCourseIdAndPathCourseIdNot(
            UUID pathId,
            UUID courseId,
            UUID pathCourseId
    );

    boolean
    existsByLearningPathPathIdAndCourseOrderAndPathCourseIdNot(
            UUID pathId,
            Integer courseOrder,
            UUID pathCourseId
    );

    long countByLearningPathPathId(
            UUID pathId
    );

    long
    countByLearningPathPathIdAndRequiredForCompletionTrue(
            UUID pathId
    );

    void deleteAllByLearningPathPathId(
            UUID pathId
    );

    @Query("""
            SELECT COUNT(lpc)
            FROM LearningPathCourse lpc
            WHERE lpc.learningPath.pathId = :pathId
              AND EXISTS (
                  SELECT e.enrollmentId
                  FROM Enrollment e
                  WHERE e.learnerId = :learnerId
                    AND e.course.courseId = lpc.course.courseId
                    AND e.status = :completedStatus
              )
            """)
    long countCompletedCoursesForLearner(
            @Param("pathId")
            UUID pathId,

            @Param("learnerId")
            UUID learnerId,

            @Param("completedStatus")
            EnrollmentStatus completedStatus
    );

    @Query("""
            SELECT COUNT(lpc)
            FROM LearningPathCourse lpc
            WHERE lpc.learningPath.pathId = :pathId
              AND lpc.requiredForCompletion = true
              AND EXISTS (
                  SELECT e.enrollmentId
                  FROM Enrollment e
                  WHERE e.learnerId = :learnerId
                    AND e.course.courseId = lpc.course.courseId
                    AND e.status = :completedStatus
              )
            """)
    long countCompletedRequiredCoursesForLearner(
            @Param("pathId")
            UUID pathId,

            @Param("learnerId")
            UUID learnerId,

            @Param("completedStatus")
            EnrollmentStatus completedStatus
    );

    @Query("""
            SELECT MIN(lpc.courseOrder)
            FROM LearningPathCourse lpc
            WHERE lpc.learningPath.pathId = :pathId
              AND NOT EXISTS (
                  SELECT e.enrollmentId
                  FROM Enrollment e
                  WHERE e.learnerId = :learnerId
                    AND e.course.courseId = lpc.course.courseId
                    AND e.status = :completedStatus
              )
            """)
    Integer findFirstIncompleteCourseOrder(
            @Param("pathId")
            UUID pathId,

            @Param("learnerId")
            UUID learnerId,

            @Param("completedStatus")
            EnrollmentStatus completedStatus
    );
}