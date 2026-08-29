package com.skillspherenexus.learningservice.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.skillspherenexus.learningservice.dto.LearningPathAssignmentRequestDTO;
import com.skillspherenexus.learningservice.dto.LearningPathAssignmentResponseDTO;
import com.skillspherenexus.learningservice.enums.LearningPathAssignmentStatus;
import com.skillspherenexus.learningservice.service.LearningPathAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api/learning-path-assignments")
@RequiredArgsConstructor
public class LearningPathAssignmentController {

    private final LearningPathAssignmentService
            learningPathAssignmentService;

    @PostMapping("/path/{pathId}")
    public ResponseEntity<LearningPathAssignmentResponseDTO>
    assignLearningPath(
            @PathVariable UUID pathId,
            @Valid
            @RequestBody
            LearningPathAssignmentRequestDTO request
    ) {
        LearningPathAssignmentResponseDTO response =
                learningPathAssignmentService
                        .assignLearningPath(
                                pathId,
                                request
                        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{assignmentId}")
    public ResponseEntity<LearningPathAssignmentResponseDTO>
    getAssignmentById(
            @PathVariable UUID assignmentId
    ) {
        LearningPathAssignmentResponseDTO response =
                learningPathAssignmentService
                        .getAssignmentById(
                                assignmentId
                        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/path/{pathId}/learner/{learnerId}")
    public ResponseEntity<LearningPathAssignmentResponseDTO>
    getAssignment(
            @PathVariable UUID pathId,
            @PathVariable UUID learnerId
    ) {
        LearningPathAssignmentResponseDTO response =
                learningPathAssignmentService
                        .getAssignment(
                                pathId,
                                learnerId
                        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/learner/{learnerId}")
    public ResponseEntity<List<LearningPathAssignmentResponseDTO>>
    getAssignmentsByLearner(
            @PathVariable UUID learnerId
    ) {
        List<LearningPathAssignmentResponseDTO> response =
                learningPathAssignmentService
                        .getAssignmentsByLearner(
                                learnerId
                        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/path/{pathId}")
    public ResponseEntity<List<LearningPathAssignmentResponseDTO>>
    getAssignmentsByPath(
            @PathVariable UUID pathId
    ) {
        List<LearningPathAssignmentResponseDTO> response =
                learningPathAssignmentService
                        .getAssignmentsByPath(
                                pathId
                        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<LearningPathAssignmentResponseDTO>>
    getAssignmentsByStatus(
            @PathVariable
            LearningPathAssignmentStatus status
    ) {
        List<LearningPathAssignmentResponseDTO> response =
                learningPathAssignmentService
                        .getAssignmentsByStatus(
                                status
                        );

        return ResponseEntity.ok(response);
    }

    @GetMapping(
            "/learner/{learnerId}/status/{status}"
    )
    public ResponseEntity<List<LearningPathAssignmentResponseDTO>>
    getLearnerAssignmentsByStatus(
            @PathVariable UUID learnerId,
            @PathVariable
            LearningPathAssignmentStatus status
    ) {
        List<LearningPathAssignmentResponseDTO> response =
                learningPathAssignmentService
                        .getLearnerAssignmentsByStatus(
                                learnerId,
                                status
                        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{assignmentId}/start")
    public ResponseEntity<LearningPathAssignmentResponseDTO>
    startLearningPath(
            @PathVariable UUID assignmentId
    ) {
        LearningPathAssignmentResponseDTO response =
                learningPathAssignmentService
                        .startLearningPath(
                                assignmentId
                        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{assignmentId}/refresh-progress")
    public ResponseEntity<LearningPathAssignmentResponseDTO>
    refreshProgress(
            @PathVariable UUID assignmentId
    ) {
        LearningPathAssignmentResponseDTO response =
                learningPathAssignmentService
                        .refreshProgress(
                                assignmentId
                        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{assignmentId}/complete")
    public ResponseEntity<LearningPathAssignmentResponseDTO>
    completeLearningPath(
            @PathVariable UUID assignmentId
    ) {
        LearningPathAssignmentResponseDTO response =
                learningPathAssignmentService
                        .completeLearningPath(
                                assignmentId
                        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{assignmentId}/cancel")
    public ResponseEntity<LearningPathAssignmentResponseDTO>
    cancelAssignment(
            @PathVariable UUID assignmentId,
            @RequestParam String reason
    ) {
        LearningPathAssignmentResponseDTO response =
                learningPathAssignmentService
                        .cancelAssignment(
                                assignmentId,
                                reason
                        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{assignmentId}/reactivate")
    public ResponseEntity<LearningPathAssignmentResponseDTO>
    reactivateAssignment(
            @PathVariable UUID assignmentId
    ) {
        LearningPathAssignmentResponseDTO response =
                learningPathAssignmentService
                        .reactivateAssignment(
                                assignmentId
                        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/count/learner/{learnerId}")
    public ResponseEntity<Long>
    countAssignmentsByLearner(
            @PathVariable UUID learnerId
    ) {
        long count =
                learningPathAssignmentService
                        .countAssignmentsByLearner(
                                learnerId
                        );

        return ResponseEntity.ok(count);
    }

    @GetMapping("/count/path/{pathId}")
    public ResponseEntity<Long>
    countAssignmentsByPath(
            @PathVariable UUID pathId
    ) {
        long count =
                learningPathAssignmentService
                        .countAssignmentsByPath(
                                pathId
                        );

        return ResponseEntity.ok(count);
    }

    @GetMapping(
            "/count/path/{pathId}/status/{status}"
    )
    public ResponseEntity<Long>
    countAssignmentsByPathAndStatus(
            @PathVariable UUID pathId,
            @PathVariable
            LearningPathAssignmentStatus status
    ) {
        long count =
                learningPathAssignmentService
                        .countAssignmentsByPathAndStatus(
                                pathId,
                                status
                        );

        return ResponseEntity.ok(count);
    }
}