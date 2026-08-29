package com.skillspherenexus.learningservice.repository;

import com.skillspherenexus.learningservice.entity.CourseModule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseModuleRepository
        extends JpaRepository<CourseModule, UUID> {

    List<CourseModule>
    findByCourseCourseIdOrderByModuleOrderAsc(
            UUID courseId
    );

    List<CourseModule>
    findByCourseCourseIdAndPublishedTrueOrderByModuleOrderAsc(
            UUID courseId
    );

    Optional<CourseModule>
    findByModuleIdAndCourseCourseId(
            UUID moduleId,
            UUID courseId
    );

    boolean existsByCourseCourseIdAndModuleOrder(
            UUID courseId,
            Integer moduleOrder
    );

    boolean existsByCourseCourseIdAndModuleOrderAndModuleIdNot(
            UUID courseId,
            Integer moduleOrder,
            UUID moduleId
    );

    long countByCourseCourseId(
            UUID courseId
    );
}
