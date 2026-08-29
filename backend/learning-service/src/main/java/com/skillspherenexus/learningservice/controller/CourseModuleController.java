package com.skillspherenexus.learningservice.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.skillspherenexus.learningservice.dto.CourseModuleCreateRequestDTO;
import com.skillspherenexus.learningservice.dto.CourseModuleResponseDTO;
import com.skillspherenexus.learningservice.dto.CourseModuleUpdateRequestDTO;
import com.skillspherenexus.learningservice.service.CourseModuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api/courses/{courseId}/modules")
@RequiredArgsConstructor
public class CourseModuleController {

    private final CourseModuleService courseModuleService;

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PostMapping
    public ResponseEntity<CourseModuleResponseDTO> createModule(
            @PathVariable UUID courseId,
            @Valid @RequestBody CourseModuleCreateRequestDTO request
    ) {
        CourseModuleResponseDTO createdModule =
                courseModuleService.createModule(
                        courseId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdModule);
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PutMapping("/{moduleId}")
    public ResponseEntity<CourseModuleResponseDTO> updateModule(
            @PathVariable UUID courseId,
            @PathVariable UUID moduleId,
            @Valid @RequestBody CourseModuleUpdateRequestDTO request
    ) {
        CourseModuleResponseDTO updatedModule =
                courseModuleService.updateModule(
                        courseId,
                        moduleId,
                        request
                );

        return ResponseEntity.ok(updatedModule);
    }

    @GetMapping
    public ResponseEntity<List<CourseModuleResponseDTO>>
    getAllModulesByCourse(
            @PathVariable UUID courseId
    ) {
        return ResponseEntity.ok(
                courseModuleService.getModulesByCourse(courseId)
        );
    }

    @GetMapping("/published")
    public ResponseEntity<List<CourseModuleResponseDTO>>
    getPublishedModulesByCourse(
            @PathVariable UUID courseId
    ) {
        return ResponseEntity.ok(
                courseModuleService
                        .getPublishedModulesByCourse(courseId)
        );
    }

    @GetMapping("/{moduleId}")
    public ResponseEntity<CourseModuleResponseDTO> getModuleById(
            @PathVariable UUID courseId,
            @PathVariable UUID moduleId
    ) {
        return ResponseEntity.ok(
                courseModuleService.getModuleById(
                        courseId,
                        moduleId
                )
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PatchMapping("/{moduleId}/publish")
    public ResponseEntity<CourseModuleResponseDTO> publishModule(
            @PathVariable UUID courseId,
            @PathVariable UUID moduleId
    ) {
        return ResponseEntity.ok(
                courseModuleService.publishModule(
                        courseId,
                        moduleId
                )
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PatchMapping("/{moduleId}/unpublish")
    public ResponseEntity<CourseModuleResponseDTO> unpublishModule(
            @PathVariable UUID courseId,
            @PathVariable UUID moduleId
    ) {
        return ResponseEntity.ok(
                courseModuleService.unpublishModule(
                        courseId,
                        moduleId
                )
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @DeleteMapping("/{moduleId}")
    public ResponseEntity<Void> deleteModule(
            @PathVariable UUID courseId,
            @PathVariable UUID moduleId
    ) {
        courseModuleService.deleteModule(
                courseId,
                moduleId
        );

        return ResponseEntity.noContent().build();
    }
}