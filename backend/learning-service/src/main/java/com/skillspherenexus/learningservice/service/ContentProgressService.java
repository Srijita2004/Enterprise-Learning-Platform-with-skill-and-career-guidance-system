package com.skillspherenexus.learningservice.service;

import com.skillspherenexus.learningservice.dto.ContentProgressResponseDTO;
import com.skillspherenexus.learningservice.dto.ContentProgressUpdateRequestDTO;
import com.skillspherenexus.learningservice.dto.CourseProgressSummaryResponseDTO;
import com.skillspherenexus.learningservice.enums.ContentProgressStatus;

import java.util.List;
import java.util.UUID;

public interface ContentProgressService {

    ContentProgressResponseDTO updateContentProgress(
            UUID enrollmentId,
            UUID contentId,
            ContentProgressUpdateRequestDTO request
    );

    ContentProgressResponseDTO getContentProgress(
            UUID enrollmentId,
            UUID contentId
    );

    List<ContentProgressResponseDTO>
    getProgressByEnrollment(
            UUID enrollmentId
    );

    List<ContentProgressResponseDTO>
    getProgressByEnrollmentAndStatus(
            UUID enrollmentId,
            ContentProgressStatus status
    );

    CourseProgressSummaryResponseDTO
    getCourseProgressSummary(
            UUID enrollmentId
    );

    boolean areAllMandatoryContentsCompleted(
            UUID enrollmentId
    );
}