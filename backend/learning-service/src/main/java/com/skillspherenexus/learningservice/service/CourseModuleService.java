package com.skillspherenexus.learningservice.service;

import com.skillspherenexus.learningservice.dto.CourseModuleCreateRequestDTO;
import com.skillspherenexus.learningservice.dto.CourseModuleResponseDTO;
import com.skillspherenexus.learningservice.dto.CourseModuleUpdateRequestDTO;

import java.util.List;
import java.util.UUID;

public interface CourseModuleService {

    CourseModuleResponseDTO createModule(
            UUID courseId,
            CourseModuleCreateRequestDTO request
    );

    CourseModuleResponseDTO updateModule(
            UUID courseId,
            UUID moduleId,
            CourseModuleUpdateRequestDTO request
    );

    CourseModuleResponseDTO getModuleById(
            UUID courseId,
            UUID moduleId
    );

    List<CourseModuleResponseDTO> getModulesByCourse(
            UUID courseId
    );

    List<CourseModuleResponseDTO> getPublishedModulesByCourse(
            UUID courseId
    );

    CourseModuleResponseDTO publishModule(
            UUID courseId,
            UUID moduleId
    );

    CourseModuleResponseDTO unpublishModule(
            UUID courseId,
            UUID moduleId
    );

    void deleteModule(
            UUID courseId,
            UUID moduleId
    );
}
