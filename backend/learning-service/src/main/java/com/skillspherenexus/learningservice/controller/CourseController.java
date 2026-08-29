package com.skillspherenexus.learningservice.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.skillspherenexus.learningservice.dto.CourseCreateRequestDTO;
import com.skillspherenexus.learningservice.dto.CourseResponseDTO;
import com.skillspherenexus.learningservice.dto.CourseUpdateRequestDTO;
import com.skillspherenexus.learningservice.enums.CourseLevel;
import com.skillspherenexus.learningservice.enums.CourseStatus;
import com.skillspherenexus.learningservice.enums.CourseType;
import com.skillspherenexus.learningservice.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PostMapping
    public ResponseEntity<CourseResponseDTO> createCourse(
            @Valid @RequestBody CourseCreateRequestDTO request
    ) {
        CourseResponseDTO createdCourse =
                courseService.createCourse(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdCourse);
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PutMapping("/{courseId}")
    public ResponseEntity<CourseResponseDTO> updateCourse(
            @PathVariable UUID courseId,
            @Valid @RequestBody CourseUpdateRequestDTO request
    ) {
        CourseResponseDTO updatedCourse =
                courseService.updateCourse(courseId, request);

        return ResponseEntity.ok(updatedCourse);
    }

    @GetMapping
    public ResponseEntity<List<CourseResponseDTO>> getAllCourses() {

        return ResponseEntity.ok(
                courseService.getAllCourses()
        );
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<CourseResponseDTO> getCourseById(
            @PathVariable UUID courseId
    ) {
        return ResponseEntity.ok(
                courseService.getCourseById(courseId)
        );
    }

    @GetMapping("/code/{courseCode}")
    public ResponseEntity<CourseResponseDTO> getCourseByCode(
            @PathVariable String courseCode
    ) {
        return ResponseEntity.ok(
                courseService.getCourseByCode(courseCode)
        );
    }

    @GetMapping("/search")
    public ResponseEntity<List<CourseResponseDTO>> searchCourses(
            @RequestParam String title
    ) {
        return ResponseEntity.ok(
                courseService.searchCoursesByTitle(title)
        );
    }

    @GetMapping("/filter/category")
    public ResponseEntity<List<CourseResponseDTO>> filterByCategory(
            @RequestParam String category
    ) {
        return ResponseEntity.ok(
                courseService.getCoursesByCategory(category)
        );
    }

    @GetMapping("/filter/type")
    public ResponseEntity<List<CourseResponseDTO>> filterByType(
            @RequestParam CourseType courseType
    ) {
        return ResponseEntity.ok(
                courseService.getCoursesByType(courseType)
        );
    }

    @GetMapping("/filter/level")
    public ResponseEntity<List<CourseResponseDTO>> filterByLevel(
            @RequestParam CourseLevel courseLevel
    ) {
        return ResponseEntity.ok(
                courseService.getCoursesByLevel(courseLevel)
        );
    }

    @GetMapping("/filter/status")
    public ResponseEntity<List<CourseResponseDTO>> filterByStatus(
            @RequestParam CourseStatus status
    ) {
        return ResponseEntity.ok(
                courseService.getCoursesByStatus(status)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PatchMapping("/{courseId}/publish")
    public ResponseEntity<CourseResponseDTO> publishCourse(
            @PathVariable UUID courseId
    ) {
        return ResponseEntity.ok(
                courseService.publishCourse(courseId)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PatchMapping("/{courseId}/archive")
    public ResponseEntity<CourseResponseDTO> archiveCourse(
            @PathVariable UUID courseId
    ) {
        return ResponseEntity.ok(
                courseService.archiveCourse(courseId)
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @DeleteMapping("/{courseId}")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable UUID courseId
    ) {
        courseService.deleteCourse(courseId);

        return ResponseEntity.noContent().build();
    }
}