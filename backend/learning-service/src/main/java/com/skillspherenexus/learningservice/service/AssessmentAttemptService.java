package com.skillspherenexus.learningservice.service;

import com.skillspherenexus.learningservice.dto.AssessmentAttemptResponseDTO;
import com.skillspherenexus.learningservice.dto.AssessmentGradeRequestDTO;
import com.skillspherenexus.learningservice.enums.AssessmentStatus;

import java.util.List;
import java.util.UUID;

public interface AssessmentAttemptService {

    AssessmentAttemptResponseDTO startAssessmentAttempt(
            UUID enrollmentId,
            UUID contentId
    );

    AssessmentAttemptResponseDTO submitAssessmentAttempt(
            UUID attemptId
    );

    AssessmentAttemptResponseDTO gradeAssessmentAttempt(
            UUID attemptId,
            AssessmentGradeRequestDTO request
    );

    AssessmentAttemptResponseDTO getAssessmentAttemptById(
            UUID attemptId
    );

    List<AssessmentAttemptResponseDTO>
    getAttemptsByEnrollment(
            UUID enrollmentId
    );

    List<AssessmentAttemptResponseDTO>
    getAttemptsByEnrollmentAndContent(
            UUID enrollmentId,
            UUID contentId
    );

    AssessmentAttemptResponseDTO getLatestAttempt(
            UUID enrollmentId,
            UUID contentId
    );

    AssessmentAttemptResponseDTO getBestPassedAttempt(
            UUID enrollmentId,
            UUID contentId
    );

    boolean hasPassedAssessment(
            UUID enrollmentId,
            UUID contentId
    );

    boolean areAllMandatoryAssessmentsPassed(
            UUID enrollmentId
    );

    long countAttemptsByEnrollment(
            UUID enrollmentId
    );

    long countAttemptsByEnrollmentAndStatus(
            UUID enrollmentId,
            AssessmentStatus status
    );
}