package com.skillspherenexus.learningservice.service;

import com.skillspherenexus.learningservice.dto.LearningPathCourseRequestDTO;
import com.skillspherenexus.learningservice.dto.LearningPathCourseResponseDTO;
import com.skillspherenexus.learningservice.dto.LearningPathCreateRequestDTO;
import com.skillspherenexus.learningservice.dto.LearningPathResponseDTO;
import com.skillspherenexus.learningservice.dto.LearningPathUpdateRequestDTO;
import com.skillspherenexus.learningservice.enums.LearningPathStatus;

import java.util.List;
import java.util.UUID;

public interface LearningPathService {

    LearningPathResponseDTO createLearningPath(
            LearningPathCreateRequestDTO request
    );

    LearningPathResponseDTO updateLearningPath(
            UUID pathId,
            LearningPathUpdateRequestDTO request
    );

    LearningPathResponseDTO getLearningPathById(
            UUID pathId
    );

    LearningPathResponseDTO getLearningPathByCode(
            String pathCode
    );

    List<LearningPathResponseDTO> getAllLearningPaths();

    List<LearningPathResponseDTO> getLearningPathsByStatus(
            LearningPathStatus status
    );

    List<LearningPathResponseDTO> getLearningPathsByCategory(
            String category
    );

    List<LearningPathResponseDTO> getLearningPathsByTargetRole(
            String targetRole
    );

    LearningPathResponseDTO publishLearningPath(
            UUID pathId
    );

    LearningPathResponseDTO archiveLearningPath(
            UUID pathId
    );

    void deleteLearningPath(
            UUID pathId
    );

    LearningPathCourseResponseDTO addCourseToLearningPath(
            UUID pathId,
            LearningPathCourseRequestDTO request
    );

    LearningPathCourseResponseDTO updateLearningPathCourse(
            UUID pathId,
            UUID pathCourseId,
            LearningPathCourseRequestDTO request
    );

    List<LearningPathCourseResponseDTO> getLearningPathCourses(
            UUID pathId
    );

    void removeCourseFromLearningPath(
            UUID pathId,
            UUID pathCourseId
    );
}