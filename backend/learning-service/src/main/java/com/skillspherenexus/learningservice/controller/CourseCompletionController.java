package com.skillspherenexus.learningservice.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.skillspherenexus.learningservice.dto.CourseCompletionResponseDTO;
import com.skillspherenexus.learningservice.service.CourseCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api")
@RequiredArgsConstructor
public class CourseCompletionController {

    private final CourseCompletionService
            courseCompletionService;

    @PostMapping(
            "/enrollments/{enrollmentId}/completion"
    )
    public ResponseEntity<CourseCompletionResponseDTO>
    completeCourse(
            @PathVariable UUID enrollmentId
    ) {
        CourseCompletionResponseDTO response =
                courseCompletionService.completeCourse(
                        enrollmentId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping(
            "/course-completions/{completionId}"
    )
    public ResponseEntity<CourseCompletionResponseDTO>
    getCompletionById(
            @PathVariable UUID completionId
    ) {
        return ResponseEntity.ok(
                courseCompletionService
                        .getCompletionById(
                                completionId
                        )
        );
    }

    @GetMapping(
            "/enrollments/{enrollmentId}/completion"
    )
    public ResponseEntity<CourseCompletionResponseDTO>
    getCompletionByEnrollment(
            @PathVariable UUID enrollmentId
    ) {
        return ResponseEntity.ok(
                courseCompletionService
                        .getCompletionByEnrollment(
                                enrollmentId
                        )
        );
    }

    @GetMapping(
            "/learners/{learnerId}/courses/{courseId}/completion"
    )
    public ResponseEntity<CourseCompletionResponseDTO>
    getCompletionByLearnerAndCourse(
            @PathVariable UUID learnerId,
            @PathVariable UUID courseId
    ) {
        return ResponseEntity.ok(
                courseCompletionService
                        .getCompletionByLearnerAndCourse(
                                learnerId,
                                courseId
                        )
        );
    }

    @GetMapping(
            "/learners/{learnerId}/course-completions"
    )
    public ResponseEntity<List<CourseCompletionResponseDTO>>
    getCompletionsByLearner(
            @PathVariable UUID learnerId
    ) {
        return ResponseEntity.ok(
                courseCompletionService
                        .getCompletionsByLearner(
                                learnerId
                        )
        );
    }

    @GetMapping(
            "/courses/{courseId}/completions"
    )
    public ResponseEntity<List<CourseCompletionResponseDTO>>
    getCompletionsByCourse(
            @PathVariable UUID courseId
    ) {
        return ResponseEntity.ok(
                courseCompletionService
                        .getCompletionsByCourse(
                                courseId
                        )
        );
    }

    @GetMapping(
            "/enrollments/{enrollmentId}/completion/status"
    )
    public ResponseEntity<Map<String, Object>>
    checkCourseCompletion(
            @PathVariable UUID enrollmentId
    ) {
        boolean completed =
                courseCompletionService
                        .isCourseCompleted(
                                enrollmentId
                        );

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "enrollmentId",
                enrollmentId
        );

        response.put(
                "courseCompleted",
                completed
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping(
            "/enrollments/{enrollmentId}/certificate-eligibility"
    )
    public ResponseEntity<Map<String, Object>>
    checkCertificateEligibility(
            @PathVariable UUID enrollmentId
    ) {
        boolean certificateEligible =
                courseCompletionService
                        .isCertificateEligible(
                                enrollmentId
                        );

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "enrollmentId",
                enrollmentId
        );

        response.put(
                "certificateEligible",
                certificateEligible
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping(
            "/learners/{learnerId}/course-completions/count"
    )
    public ResponseEntity<Map<String, Object>>
    countLearnerCompletions(
            @PathVariable UUID learnerId
    ) {
        long count =
                courseCompletionService
                        .countCompletionsByLearner(
                                learnerId
                        );

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "learnerId",
                learnerId
        );

        response.put(
                "completedCourseCount",
                count
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping(
            "/courses/{courseId}/completions/count"
    )
    public ResponseEntity<Map<String, Object>>
    countCourseCompletions(
            @PathVariable UUID courseId
    ) {
        long completionCount =
                courseCompletionService
                        .countCompletionsByCourse(
                                courseId
                        );

        long certificateEligibleCount =
                courseCompletionService
                        .countCertificateEligibleCompletionsByCourse(
                                courseId
                        );

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "courseId",
                courseId
        );

        response.put(
                "completionCount",
                completionCount
        );

        response.put(
                "certificateEligibleCount",
                certificateEligibleCount
        );

        return ResponseEntity.ok(response);
    }
}