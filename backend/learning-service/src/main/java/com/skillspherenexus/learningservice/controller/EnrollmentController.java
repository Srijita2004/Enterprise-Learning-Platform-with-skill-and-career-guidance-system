package com.skillspherenexus.learningservice.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.skillspherenexus.learningservice.dto.EnrollmentCancellationRequestDTO;
import com.skillspherenexus.learningservice.dto.EnrollmentCreateRequestDTO;
import com.skillspherenexus.learningservice.dto.EnrollmentResponseDTO;
import com.skillspherenexus.learningservice.dto.PaymentRejectionRequestDTO;
import com.skillspherenexus.learningservice.dto.PaymentSubmissionRequestDTO;
import com.skillspherenexus.learningservice.enums.EnrollmentStatus;
import com.skillspherenexus.learningservice.enums.PaymentStatus;
import com.skillspherenexus.learningservice.service.EnrollmentService;
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
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/courses/{courseId}/enrollments")
    public ResponseEntity<EnrollmentResponseDTO> createEnrollment(
            @PathVariable UUID courseId,
            @Valid @RequestBody EnrollmentCreateRequestDTO request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(enrollmentService.createEnrollment(courseId, request));
    }

    @GetMapping("/enrollments/{enrollmentId}")
    public ResponseEntity<EnrollmentResponseDTO> getEnrollmentById(
            @PathVariable UUID enrollmentId
    ) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentById(enrollmentId));
    }

    @GetMapping("/learners/{learnerId}/courses/{courseId}/enrollment")
    public ResponseEntity<EnrollmentResponseDTO> getEnrollmentByLearnerAndCourse(
            @PathVariable UUID learnerId,
            @PathVariable UUID courseId
    ) {
        return ResponseEntity.ok(
                enrollmentService.getEnrollmentByLearnerAndCourse(learnerId, courseId)
        );
    }

    @GetMapping("/learners/{learnerId}/enrollments")
    public ResponseEntity<List<EnrollmentResponseDTO>> getLearnerEnrollments(
            @PathVariable UUID learnerId,
            @RequestParam(required = false) String status
    ) {
        if (status == null || status.isBlank()) {
            return ResponseEntity.ok(enrollmentService.getEnrollmentsByLearner(learnerId));
        }
        return ResponseEntity.ok(
                enrollmentService.getLearnerEnrollmentsByStatus(
                        learnerId,
                        parseEnrollmentStatus(status)
                )
        );
    }

    @GetMapping("/courses/{courseId}/enrollments")
    public ResponseEntity<List<EnrollmentResponseDTO>> getCourseEnrollments(
            @PathVariable UUID courseId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        requireManagerRole(userRole);
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByCourse(courseId));
    }

    @GetMapping("/enrollments")
    public ResponseEntity<List<EnrollmentResponseDTO>> getEnrollments(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        requireManagerRole(userRole);

        if (paymentStatus != null && !paymentStatus.isBlank()) {
            return ResponseEntity.ok(
                    enrollmentService.getEnrollmentsByPaymentStatus(
                            parsePaymentStatus(paymentStatus)
                    )
            );
        }
        if (status != null && !status.isBlank()) {
            return ResponseEntity.ok(
                    enrollmentService.getEnrollmentsByStatus(
                            parseEnrollmentStatus(status)
                    )
            );
        }
        return ResponseEntity.ok(enrollmentService.getAllEnrollments());
    }

    @GetMapping("/payments/pending")
    public ResponseEntity<List<EnrollmentResponseDTO>> getPendingPayments(
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        requireManagerRole(userRole);
        return ResponseEntity.ok(
                enrollmentService.getEnrollmentsByPaymentStatus(PaymentStatus.PENDING)
        );
    }

    @PatchMapping("/enrollments/{enrollmentId}/payment/submit")
    public ResponseEntity<EnrollmentResponseDTO> submitPayment(
            @PathVariable UUID enrollmentId,
            @Valid @RequestBody PaymentSubmissionRequestDTO request
    ) {
        return ResponseEntity.ok(enrollmentService.submitPayment(enrollmentId, request));
    }

    @PatchMapping("/enrollments/{enrollmentId}/payment/approve")
    public ResponseEntity<EnrollmentResponseDTO> approvePayment(
            @PathVariable UUID enrollmentId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId
    ) {
        requireManagerRole(userRole);
        return ResponseEntity.ok(enrollmentService.approvePayment(enrollmentId, userId));
    }

    @PatchMapping("/enrollments/{enrollmentId}/payment/reject")
    public ResponseEntity<EnrollmentResponseDTO> rejectPayment(
            @PathVariable UUID enrollmentId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @Valid @RequestBody PaymentRejectionRequestDTO request
    ) {
        requireManagerRole(userRole);
        return ResponseEntity.ok(
                enrollmentService.rejectPayment(enrollmentId, userId, request)
        );
    }

    /** Backward-compatible alias for existing Postman requests. */
    @PatchMapping("/enrollments/{enrollmentId}/payment/confirm")
    public ResponseEntity<EnrollmentResponseDTO> confirmPayment(
            @PathVariable UUID enrollmentId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId
    ) {
        requireManagerRole(userRole);
        return ResponseEntity.ok(enrollmentService.approvePayment(enrollmentId, userId));
    }

    @PatchMapping("/enrollments/{enrollmentId}/cancel")
    public ResponseEntity<EnrollmentResponseDTO> cancelEnrollment(
            @PathVariable UUID enrollmentId,
            @Valid @RequestBody EnrollmentCancellationRequestDTO request
    ) {
        return ResponseEntity.ok(
                enrollmentService.cancelEnrollment(enrollmentId, request)
        );
    }

    @PatchMapping("/enrollments/{enrollmentId}/complete")
    public ResponseEntity<EnrollmentResponseDTO> completeEnrollment(
            @PathVariable UUID enrollmentId
    ) {
        return ResponseEntity.ok(enrollmentService.completeEnrollment(enrollmentId));
    }

    @PatchMapping("/enrollments/{enrollmentId}/expire")
    public ResponseEntity<EnrollmentResponseDTO> expireEnrollment(
            @PathVariable UUID enrollmentId
    ) {
        return ResponseEntity.ok(enrollmentService.expireEnrollment(enrollmentId));
    }

    @PatchMapping("/enrollments/{enrollmentId}/access")
    public ResponseEntity<EnrollmentResponseDTO> recordCourseAccess(
            @PathVariable UUID enrollmentId
    ) {
        return ResponseEntity.ok(enrollmentService.recordCourseAccess(enrollmentId));
    }

    @GetMapping("/learners/{learnerId}/courses/{courseId}/access")
    public ResponseEntity<Map<String, Object>> checkCourseAccess(
            @PathVariable UUID learnerId,
            @PathVariable UUID courseId
    ) {
        boolean accessAllowed = enrollmentService.hasActiveCourseAccess(learnerId, courseId);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("learnerId", learnerId);
        response.put("courseId", courseId);
        response.put("accessAllowed", accessAllowed);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/courses/{courseId}/enrollments/count")
    public ResponseEntity<Map<String, Object>> countCourseEnrollments(
            @PathVariable UUID courseId,
            @RequestParam(required = false) String status
    ) {
        long count;
        String statusValue;

        if (status == null || status.isBlank()) {
            count = enrollmentService.countEnrollmentsByCourse(courseId);
            statusValue = "ALL";
        } else {
            EnrollmentStatus enrollmentStatus = parseEnrollmentStatus(status);
            count = enrollmentService.countEnrollmentsByCourseAndStatus(
                    courseId,
                    enrollmentStatus
            );
            statusValue = enrollmentStatus.name();
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("courseId", courseId);
        response.put("status", statusValue);
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    private void requireManagerRole(String role) {
        String normalizedRole = role == null
                ? ""
                : role.trim().toUpperCase(Locale.ROOT);
        if (!normalizedRole.equals("ADMIN") && !normalizedRole.equals("HR")) {
            throw new IllegalArgumentException("Only Admin or HR can perform this action");
        }
    }

    private EnrollmentStatus parseEnrollmentStatus(String status) {
        try {
            return EnrollmentStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException(
                    "Invalid enrollment status: " + status
                            + ". Allowed values are PENDING, ACTIVE, COMPLETED, CANCELLED, EXPIRED"
            );
        }
    }

    private PaymentStatus parsePaymentStatus(String status) {
        try {
            return PaymentStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException(
                    "Invalid payment status: " + status
                            + ". Allowed values are NOT_REQUIRED, PENDING, PAID, FAILED, REJECTED, REFUNDED"
            );
        }
    }
}
