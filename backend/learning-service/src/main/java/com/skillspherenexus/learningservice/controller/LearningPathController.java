package com.skillspherenexus.learningservice.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.skillspherenexus.learningservice.dto.LearningPathCourseRequestDTO;
import com.skillspherenexus.learningservice.dto.LearningPathCourseResponseDTO;
import com.skillspherenexus.learningservice.dto.LearningPathCreateRequestDTO;
import com.skillspherenexus.learningservice.dto.LearningPathResponseDTO;
import com.skillspherenexus.learningservice.dto.LearningPathUpdateRequestDTO;
import com.skillspherenexus.learningservice.enums.LearningPathStatus;
import com.skillspherenexus.learningservice.service.LearningPathService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api/learning-paths")
@RequiredArgsConstructor
public class LearningPathController {

    private final LearningPathService learningPathService;

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PostMapping
    public ResponseEntity<LearningPathResponseDTO>
    createLearningPath(
            @Valid
            @RequestBody
            LearningPathCreateRequestDTO request
    ) {
        LearningPathResponseDTO response =
                learningPathService.createLearningPath(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PutMapping("/{pathId}")
    public ResponseEntity<LearningPathResponseDTO>
    updateLearningPath(
            @PathVariable UUID pathId,
            @Valid
            @RequestBody
            LearningPathUpdateRequestDTO request
    ) {
        LearningPathResponseDTO response =
                learningPathService.updateLearningPath(
                        pathId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{pathId}")
    public ResponseEntity<LearningPathResponseDTO>
    getLearningPathById(
            @PathVariable UUID pathId
    ) {
        return ResponseEntity.ok(
                learningPathService
                        .getLearningPathById(pathId)
        );
    }

    @GetMapping("/code/{pathCode}")
    public ResponseEntity<LearningPathResponseDTO>
    getLearningPathByCode(
            @PathVariable String pathCode
    ) {
        return ResponseEntity.ok(
                learningPathService
                        .getLearningPathByCode(pathCode)
        );
    }

    @GetMapping
    public ResponseEntity<List<LearningPathResponseDTO>>
    getAllLearningPaths() {
        return ResponseEntity.ok(
                learningPathService
                        .getAllLearningPaths()
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<LearningPathResponseDTO>>
    getLearningPathsByStatus(
            @PathVariable
            LearningPathStatus status
    ) {
        return ResponseEntity.ok(
                learningPathService
                        .getLearningPathsByStatus(status)
        );
    }

    @GetMapping("/category")
    public ResponseEntity<List<LearningPathResponseDTO>>
    getLearningPathsByCategory(
            @RequestParam String category
    ) {
        return ResponseEntity.ok(
                learningPathService
                        .getLearningPathsByCategory(category)
        );
    }

    @GetMapping("/target-role")
    public ResponseEntity<List<LearningPathResponseDTO>>
    getLearningPathsByTargetRole(
            @RequestParam String targetRole
    ) {
        return ResponseEntity.ok(
                learningPathService
                        .getLearningPathsByTargetRole(targetRole)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PostMapping("/{pathId}/publish")
    public ResponseEntity<LearningPathResponseDTO>
    publishLearningPath(
            @PathVariable UUID pathId
    ) {
        return ResponseEntity.ok(
                learningPathService
                        .publishLearningPath(pathId)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PostMapping("/{pathId}/archive")
    public ResponseEntity<LearningPathResponseDTO>
    archiveLearningPath(
            @PathVariable UUID pathId
    ) {
        return ResponseEntity.ok(
                learningPathService
                        .archiveLearningPath(pathId)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @DeleteMapping("/{pathId}")
    public ResponseEntity<Void>
    deleteLearningPath(
            @PathVariable UUID pathId
    ) {
        learningPathService.deleteLearningPath(
                pathId
        );

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PostMapping("/{pathId}/courses")
    public ResponseEntity<LearningPathCourseResponseDTO>
    addCourseToLearningPath(
            @PathVariable UUID pathId,
            @Valid
            @RequestBody
            LearningPathCourseRequestDTO request
    ) {
        LearningPathCourseResponseDTO response =
                learningPathService
                        .addCourseToLearningPath(
                                pathId,
                                request
                        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PutMapping("/{pathId}/courses/{pathCourseId}")
    public ResponseEntity<LearningPathCourseResponseDTO>
    updateLearningPathCourse(
            @PathVariable UUID pathId,
            @PathVariable UUID pathCourseId,
            @Valid
            @RequestBody
            LearningPathCourseRequestDTO request
    ) {
        LearningPathCourseResponseDTO response =
                learningPathService
                        .updateLearningPathCourse(
                                pathId,
                                pathCourseId,
                                request
                        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{pathId}/courses")
    public ResponseEntity<List<LearningPathCourseResponseDTO>>
    getLearningPathCourses(
            @PathVariable UUID pathId
    ) {
        return ResponseEntity.ok(
                learningPathService
                        .getLearningPathCourses(pathId)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @DeleteMapping("/{pathId}/courses/{pathCourseId}")
    public ResponseEntity<Void>
    removeCourseFromLearningPath(
            @PathVariable UUID pathId,
            @PathVariable UUID pathCourseId
    ) {
        learningPathService
                .removeCourseFromLearningPath(
                        pathId,
                        pathCourseId
                );

        return ResponseEntity.noContent().build();
    }
}