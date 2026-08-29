package com.skillspherenexus.learningservice.service;

import com.skillspherenexus.learningservice.dto.CourseContentCreateRequestDTO;
import com.skillspherenexus.learningservice.dto.CourseContentResponseDTO;
import com.skillspherenexus.learningservice.dto.CourseContentUpdateRequestDTO;

import java.util.List;
import java.util.UUID;

public interface CourseContentService {

    CourseContentResponseDTO createContent(
            UUID courseId,
            UUID moduleId,
            CourseContentCreateRequestDTO request
    );

    CourseContentResponseDTO updateContent(
            UUID courseId,
            UUID moduleId,
            UUID contentId,
            CourseContentUpdateRequestDTO request
    );

    CourseContentResponseDTO getContentById(
            UUID courseId,
            UUID moduleId,
            UUID contentId
    );

    List<CourseContentResponseDTO> getContentsByModule(
            UUID courseId,
            UUID moduleId
    );

    List<CourseContentResponseDTO> getPublishedContentsByModule(
            UUID courseId,
            UUID moduleId
    );

    List<CourseContentResponseDTO>
    getPublishedContentsByModuleForLearner(
            UUID courseId,
            UUID moduleId,
            UUID learnerId
    );

    CourseContentResponseDTO
    getPublishedContentByIdForLearner(
            UUID courseId,
            UUID moduleId,
            UUID contentId,
            UUID learnerId
    );

    CourseContentResponseDTO publishContent(
            UUID courseId,
            UUID moduleId,
            UUID contentId
    );

    CourseContentResponseDTO unpublishContent(
            UUID courseId,
            UUID moduleId,
            UUID contentId
    );

    void deleteContent(
            UUID courseId,
            UUID moduleId,
            UUID contentId
    );
}