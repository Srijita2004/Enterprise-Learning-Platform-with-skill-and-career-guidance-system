package com.skillspherenexus.learningservice.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.skillspherenexus.learningservice.dto.AssessmentAttemptResponseDTO;
import com.skillspherenexus.learningservice.dto.AssessmentGradeRequestDTO;
import com.skillspherenexus.learningservice.enums.AssessmentStatus;
import com.skillspherenexus.learningservice.service.AssessmentAttemptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api")
@RequiredArgsConstructor
public class AssessmentAttemptController {

    private final AssessmentAttemptService
            assessmentAttemptService;

    @PostMapping(
            "/enrollments/{enrollmentId}/assessments/{contentId}/attempts"
    )
    public ResponseEntity<AssessmentAttemptResponseDTO>
    startAssessmentAttempt(
            @PathVariable UUID enrollmentId,
            @PathVariable UUID contentId
    ) {
        AssessmentAttemptResponseDTO response =
                assessmentAttemptService
                        .startAssessmentAttempt(
                                enrollmentId,
                                contentId
                        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PatchMapping(
            "/assessment-attempts/{attemptId}/submit"
    )
    public ResponseEntity<AssessmentAttemptResponseDTO>
    submitAssessmentAttempt(
            @PathVariable UUID attemptId
    ) {
        return ResponseEntity.ok(
                assessmentAttemptService
                        .submitAssessmentAttempt(
                                attemptId
                        )
        );
    }

    @PatchMapping(
            "/assessment-attempts/{attemptId}/grade"
    )
    public ResponseEntity<AssessmentAttemptResponseDTO>
    gradeAssessmentAttempt(
            @PathVariable UUID attemptId,
            @Valid
            @RequestBody
            AssessmentGradeRequestDTO request
    ) {
        return ResponseEntity.ok(
                assessmentAttemptService
                        .gradeAssessmentAttempt(
                                attemptId,
                                request
                        )
        );
    }

    @GetMapping(
            "/assessment-attempts/{attemptId}"
    )
    public ResponseEntity<AssessmentAttemptResponseDTO>
    getAssessmentAttemptById(
            @PathVariable UUID attemptId
    ) {
        return ResponseEntity.ok(
                assessmentAttemptService
                        .getAssessmentAttemptById(
                                attemptId
                        )
        );
    }

    @GetMapping(
            "/enrollments/{enrollmentId}/assessment-attempts"
    )
    public ResponseEntity<List<AssessmentAttemptResponseDTO>>
    getAttemptsByEnrollment(
            @PathVariable UUID enrollmentId
    ) {
        return ResponseEntity.ok(
                assessmentAttemptService
                        .getAttemptsByEnrollment(
                                enrollmentId
                        )
        );
    }

    @GetMapping(
            "/enrollments/{enrollmentId}/assessments/{contentId}/attempts"
    )
    public ResponseEntity<List<AssessmentAttemptResponseDTO>>
    getAttemptsByEnrollmentAndContent(
            @PathVariable UUID enrollmentId,
            @PathVariable UUID contentId
    ) {
        return ResponseEntity.ok(
                assessmentAttemptService
                        .getAttemptsByEnrollmentAndContent(
                                enrollmentId,
                                contentId
                        )
        );
    }

    @GetMapping(
            "/enrollments/{enrollmentId}/assessments/{contentId}/attempts/latest"
    )
    public ResponseEntity<AssessmentAttemptResponseDTO>
    getLatestAttempt(
            @PathVariable UUID enrollmentId,
            @PathVariable UUID contentId
    ) {
        return ResponseEntity.ok(
                assessmentAttemptService
                        .getLatestAttempt(
                                enrollmentId,
                                contentId
                        )
        );
    }

    @GetMapping(
            "/enrollments/{enrollmentId}/assessments/{contentId}/attempts/best-passed"
    )
    public ResponseEntity<AssessmentAttemptResponseDTO>
    getBestPassedAttempt(
            @PathVariable UUID enrollmentId,
            @PathVariable UUID contentId
    ) {
        return ResponseEntity.ok(
                assessmentAttemptService
                        .getBestPassedAttempt(
                                enrollmentId,
                                contentId
                        )
        );
    }

    @GetMapping(
            "/enrollments/{enrollmentId}/assessments/{contentId}/passed"
    )
    public ResponseEntity<Map<String, Object>>
    checkPassedAssessment(
            @PathVariable UUID enrollmentId,
            @PathVariable UUID contentId
    ) {
        boolean passed =
                assessmentAttemptService
                        .hasPassedAssessment(
                                enrollmentId,
                                contentId
                        );

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put("enrollmentId", enrollmentId);
        response.put("contentId", contentId);
        response.put("passed", passed);

        return ResponseEntity.ok(response);
    }

    @GetMapping(
            "/enrollments/{enrollmentId}/assessments/mandatory/passed"
    )
    public ResponseEntity<Map<String, Object>>
    checkAllMandatoryAssessmentsPassed(
            @PathVariable UUID enrollmentId
    ) {
        boolean allPassed =
                assessmentAttemptService
                        .areAllMandatoryAssessmentsPassed(
                                enrollmentId
                        );

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put("enrollmentId", enrollmentId);
        response.put(
                "allMandatoryAssessmentsPassed",
                allPassed
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping(
            "/enrollments/{enrollmentId}/assessment-attempts/count"
    )
    public ResponseEntity<Map<String, Object>>
    countAssessmentAttempts(
            @PathVariable UUID enrollmentId,
            @RequestParam(required = false)
            String status
    ) {
        long count;
        String statusValue;

        if (status == null || status.isBlank()) {
            count = assessmentAttemptService
                    .countAttemptsByEnrollment(
                            enrollmentId
                    );

            statusValue = "ALL";
        } else {
            AssessmentStatus assessmentStatus =
                    parseAssessmentStatus(status);

            count = assessmentAttemptService
                    .countAttemptsByEnrollmentAndStatus(
                            enrollmentId,
                            assessmentStatus
                    );

            statusValue = assessmentStatus.name();
        }

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put("enrollmentId", enrollmentId);
        response.put("status", statusValue);
        response.put("count", count);

        return ResponseEntity.ok(response);
    }

    private AssessmentStatus parseAssessmentStatus(
            String status
    ) {
        try {
            return AssessmentStatus.valueOf(
                    status.trim()
                            .toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid assessment status: "
                            + status
                            + ". Allowed values are: "
                            + "NOT_ATTEMPTED, IN_PROGRESS, "
                            + "SUBMITTED, PASSED, FAILED"
            );
        }
    }
}