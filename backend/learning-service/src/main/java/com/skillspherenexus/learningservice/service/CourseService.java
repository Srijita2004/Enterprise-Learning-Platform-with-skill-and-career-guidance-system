package com.skillspherenexus.learningservice.service;

import com.skillspherenexus.learningservice.dto.CourseCreateRequestDTO;
import com.skillspherenexus.learningservice.dto.CourseResponseDTO;
import com.skillspherenexus.learningservice.dto.CourseUpdateRequestDTO;
import com.skillspherenexus.learningservice.enums.CourseLevel;
import com.skillspherenexus.learningservice.enums.CourseStatus;
import com.skillspherenexus.learningservice.enums.CourseType;

import java.util.List;
import java.util.UUID;

public interface CourseService {

    CourseResponseDTO createCourse(CourseCreateRequestDTO request);

    CourseResponseDTO updateCourse(
            UUID courseId,
            CourseUpdateRequestDTO request
    );

    CourseResponseDTO getCourseById(UUID courseId);

    CourseResponseDTO getCourseByCode(String courseCode);

    List<CourseResponseDTO> getAllCourses();

    List<CourseResponseDTO> searchCoursesByTitle(String title);

    List<CourseResponseDTO> getCoursesByCategory(String category);

    List<CourseResponseDTO> getCoursesByType(CourseType courseType);

    List<CourseResponseDTO> getCoursesByLevel(CourseLevel courseLevel);

    List<CourseResponseDTO> getCoursesByStatus(CourseStatus status);

    CourseResponseDTO publishCourse(UUID courseId);

    CourseResponseDTO archiveCourse(UUID courseId);

    void deleteCourse(UUID courseId);
}