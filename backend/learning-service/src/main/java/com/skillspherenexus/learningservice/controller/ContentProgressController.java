package com.skillspherenexus.learningservice.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.skillspherenexus.learningservice.dto.ContentProgressResponseDTO;
import com.skillspherenexus.learningservice.dto.ContentProgressUpdateRequestDTO;
import com.skillspherenexus.learningservice.dto.CourseProgressSummaryResponseDTO;
import com.skillspherenexus.learningservice.enums.ContentProgressStatus;
import com.skillspherenexus.learningservice.service.ContentProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api/enrollments/{enrollmentId}/progress")
@RequiredArgsConstructor
public class ContentProgressController {

    private final ContentProgressService contentProgressService;

    @PatchMapping("/contents/{contentId}")
    public ResponseEntity<ContentProgressResponseDTO>
    updateContentProgress(
            @PathVariable UUID enrollmentId,
            @PathVariable UUID contentId,
            @Valid
            @RequestBody
            ContentProgressUpdateRequestDTO request
    ) {
        return ResponseEntity.ok(
                contentProgressService.updateContentProgress(
                        enrollmentId,
                        contentId,
                        request
                )
        );
    }

    @GetMapping("/contents/{contentId}")
    public ResponseEntity<ContentProgressResponseDTO>
    getContentProgress(
            @PathVariable UUID enrollmentId,
            @PathVariable UUID contentId
    ) {
        return ResponseEntity.ok(
                contentProgressService.getContentProgress(
                        enrollmentId,
                        contentId
                )
        );
    }

    @GetMapping
    public ResponseEntity<List<ContentProgressResponseDTO>>
    getProgressByEnrollment(
            @PathVariable UUID enrollmentId,
            @RequestParam(required = false) String status
    ) {
        if (status == null || status.isBlank()) {
            return ResponseEntity.ok(
                    contentProgressService
                            .getProgressByEnrollment(enrollmentId)
            );
        }

        ContentProgressStatus progressStatus =
                parseProgressStatus(status);

        return ResponseEntity.ok(
                contentProgressService
                        .getProgressByEnrollmentAndStatus(
                                enrollmentId,
                                progressStatus
                        )
        );
    }

    @GetMapping("/summary")
    public ResponseEntity<CourseProgressSummaryResponseDTO>
    getCourseProgressSummary(
            @PathVariable UUID enrollmentId
    ) {
        return ResponseEntity.ok(
                contentProgressService
                        .getCourseProgressSummary(enrollmentId)
        );
    }

    private ContentProgressStatus parseProgressStatus(
            String status
    ) {
        try {
            return ContentProgressStatus.valueOf(
                    status.trim()
                            .toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid content progress status: "
                            + status
                            + ". Allowed values are: "
                            + "NOT_STARTED, IN_PROGRESS, COMPLETED"
            );
        }
    }
}