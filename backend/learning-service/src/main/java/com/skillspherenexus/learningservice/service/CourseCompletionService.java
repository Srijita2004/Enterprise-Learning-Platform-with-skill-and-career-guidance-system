package com.skillspherenexus.learningservice.service;

import com.skillspherenexus.learningservice.dto.CourseCompletionResponseDTO;

import java.util.List;
import java.util.UUID;

public interface CourseCompletionService {

    CourseCompletionResponseDTO completeCourse(
            UUID enrollmentId
    );

    CourseCompletionResponseDTO getCompletionById(
            UUID completionId
    );

    CourseCompletionResponseDTO getCompletionByEnrollment(
            UUID enrollmentId
    );

    CourseCompletionResponseDTO getCompletionByLearnerAndCourse(
            UUID learnerId,
            UUID courseId
    );

    List<CourseCompletionResponseDTO> getCompletionsByLearner(
            UUID learnerId
    );

    List<CourseCompletionResponseDTO> getCompletionsByCourse(
            UUID courseId
    );

    boolean isCourseCompleted(
            UUID enrollmentId
    );

    boolean isCertificateEligible(
            UUID enrollmentId
    );

    long countCompletionsByLearner(
            UUID learnerId
    );

    long countCompletionsByCourse(
            UUID courseId
    );

    long countCertificateEligibleCompletionsByCourse(
            UUID courseId
    );
}