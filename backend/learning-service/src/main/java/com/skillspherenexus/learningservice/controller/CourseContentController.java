package com.skillspherenexus.learningservice.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.skillspherenexus.learningservice.dto.CourseContentCreateRequestDTO;
import com.skillspherenexus.learningservice.dto.CourseContentResponseDTO;
import com.skillspherenexus.learningservice.dto.CourseContentUpdateRequestDTO;
import com.skillspherenexus.learningservice.service.CourseContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping(
        "/api/courses/{courseId}/modules/{moduleId}/contents"
)
@RequiredArgsConstructor
public class CourseContentController {

    private final CourseContentService courseContentService;

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PostMapping
    public ResponseEntity<CourseContentResponseDTO> createContent(
            @PathVariable UUID courseId,
            @PathVariable UUID moduleId,
            @Valid
            @RequestBody
            CourseContentCreateRequestDTO request
    ) {
        CourseContentResponseDTO createdContent =
                courseContentService.createContent(
                        courseId,
                        moduleId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdContent);
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PutMapping("/{contentId}")
    public ResponseEntity<CourseContentResponseDTO> updateContent(
            @PathVariable UUID courseId,
            @PathVariable UUID moduleId,
            @PathVariable UUID contentId,
            @Valid
            @RequestBody
            CourseContentUpdateRequestDTO request
    ) {
        CourseContentResponseDTO updatedContent =
                courseContentService.updateContent(
                        courseId,
                        moduleId,
                        contentId,
                        request
                );

        return ResponseEntity.ok(updatedContent);
    }

    @GetMapping
    public ResponseEntity<List<CourseContentResponseDTO>>
    getAllContentsByModule(
            @PathVariable UUID courseId,
            @PathVariable UUID moduleId
    ) {
        return ResponseEntity.ok(
                courseContentService.getContentsByModule(
                        courseId,
                        moduleId
                )
        );
    }

    @GetMapping("/published")
    public ResponseEntity<List<CourseContentResponseDTO>>
    getPublishedContentsByModule(
            @PathVariable UUID courseId,
            @PathVariable UUID moduleId
    ) {
        return ResponseEntity.ok(
                courseContentService
                        .getPublishedContentsByModule(
                                courseId,
                                moduleId
                        )
        );
    }

    @GetMapping("/published/learner/{learnerId}")
    public ResponseEntity<List<CourseContentResponseDTO>>
    getPublishedContentsByModuleForLearner(
            @PathVariable UUID courseId,
            @PathVariable UUID moduleId,
            @PathVariable UUID learnerId
    ) {
        return ResponseEntity.ok(
                courseContentService
                        .getPublishedContentsByModuleForLearner(
                                courseId,
                                moduleId,
                                learnerId
                        )
        );
    }

    @GetMapping(
            "/published/learner/{learnerId}/{contentId}"
    )
    public ResponseEntity<CourseContentResponseDTO>
    getPublishedContentByIdForLearner(
            @PathVariable UUID courseId,
            @PathVariable UUID moduleId,
            @PathVariable UUID learnerId,
            @PathVariable UUID contentId
    ) {
        return ResponseEntity.ok(
                courseContentService
                        .getPublishedContentByIdForLearner(
                                courseId,
                                moduleId,
                                contentId,
                                learnerId
                        )
        );
    }

    @GetMapping("/{contentId}")
    public ResponseEntity<CourseContentResponseDTO> getContentById(
            @PathVariable UUID courseId,
            @PathVariable UUID moduleId,
            @PathVariable UUID contentId
    ) {
        return ResponseEntity.ok(
                courseContentService.getContentById(
                        courseId,
                        moduleId,
                        contentId
                )
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PatchMapping("/{contentId}/publish")
    public ResponseEntity<CourseContentResponseDTO> publishContent(
            @PathVariable UUID courseId,
            @PathVariable UUID moduleId,
            @PathVariable UUID contentId
    ) {
        return ResponseEntity.ok(
                courseContentService.publishContent(
                        courseId,
                        moduleId,
                        contentId
                )
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PatchMapping("/{contentId}/unpublish")
    public ResponseEntity<CourseContentResponseDTO> unpublishContent(
            @PathVariable UUID courseId,
            @PathVariable UUID moduleId,
            @PathVariable UUID contentId
    ) {
        return ResponseEntity.ok(
                courseContentService.unpublishContent(
                        courseId,
                        moduleId,
                        contentId
                )
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @DeleteMapping("/{contentId}")
    public ResponseEntity<Void> deleteContent(
            @PathVariable UUID courseId,
            @PathVariable UUID moduleId,
            @PathVariable UUID contentId
    ) {
        courseContentService.deleteContent(
                courseId,
                moduleId,
                contentId
        );

        return ResponseEntity.noContent().build();
    }
}