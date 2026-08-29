package com.skillspherenexus.learningservice.repository;

import com.skillspherenexus.learningservice.entity.Course;
import com.skillspherenexus.learningservice.enums.CourseLevel;
import com.skillspherenexus.learningservice.enums.CourseStatus;
import com.skillspherenexus.learningservice.enums.CourseType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {

    boolean existsByCourseCodeIgnoreCase(String courseCode);

    Optional<Course> findByCourseCodeIgnoreCase(String courseCode);

    List<Course> findByTitleContainingIgnoreCase(String title);

    List<Course> findByCategoryIgnoreCase(String category);

    List<Course> findByCourseType(CourseType courseType);

    List<Course> findByCourseLevel(CourseLevel courseLevel);

    List<Course> findByStatus(CourseStatus status);

    long countByStatus(CourseStatus status);
}