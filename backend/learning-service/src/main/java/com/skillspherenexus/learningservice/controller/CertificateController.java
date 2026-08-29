package com.skillspherenexus.learningservice.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.skillspherenexus.learningservice.dto.CertificateIssueRequestDTO;
import com.skillspherenexus.learningservice.dto.CertificateResponseDTO;
import com.skillspherenexus.learningservice.dto.CertificateRevocationRequestDTO;
import com.skillspherenexus.learningservice.enums.CertificateStatus;
import com.skillspherenexus.learningservice.service.CertificateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
public class CertificateController {

    private final CertificateService certificateService;

    @PostMapping(
            "/course-completions/{completionId}/certificate"
    )
    public ResponseEntity<CertificateResponseDTO>
    issueCertificate(
            @PathVariable UUID completionId,
            @Valid
            @RequestBody
            CertificateIssueRequestDTO request
    ) {
        CertificateResponseDTO response =
                certificateService.issueCertificate(
                        completionId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/certificates/{certificateId}")
    public ResponseEntity<CertificateResponseDTO>
    getCertificateById(
            @PathVariable UUID certificateId
    ) {
        return ResponseEntity.ok(
                certificateService.getCertificateById(
                        certificateId
                )
        );
    }

    @GetMapping(
            "/course-completions/{completionId}/certificate"
    )
    public ResponseEntity<CertificateResponseDTO>
    getCertificateByCompletion(
            @PathVariable UUID completionId
    ) {
        return ResponseEntity.ok(
                certificateService
                        .getCertificateByCompletion(
                                completionId
                        )
        );
    }

    @GetMapping(
            "/certificates/number/{certificateNumber}"
    )
    public ResponseEntity<CertificateResponseDTO>
    getCertificateByNumber(
            @PathVariable String certificateNumber
    ) {
        return ResponseEntity.ok(
                certificateService
                        .getCertificateByNumber(
                                certificateNumber
                        )
        );
    }

    @GetMapping(
            "/certificates/verification/{verificationCode}"
    )
    public ResponseEntity<CertificateResponseDTO>
    verifyCertificate(
            @PathVariable UUID verificationCode
    ) {
        return ResponseEntity.ok(
                certificateService.verifyCertificate(
                        verificationCode
                )
        );
    }

    @GetMapping(
            "/certificates/verification/{verificationCode}/valid"
    )
    public ResponseEntity<Map<String, Object>>
    checkCertificateValidity(
            @PathVariable UUID verificationCode
    ) {
        boolean valid =
                certificateService.isCertificateValid(
                        verificationCode
                );

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "verificationCode",
                verificationCode
        );

        response.put(
                "valid",
                valid
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping(
            "/learners/{learnerId}/certificates"
    )
    public ResponseEntity<List<CertificateResponseDTO>>
    getCertificatesByLearner(
            @PathVariable UUID learnerId
    ) {
        return ResponseEntity.ok(
                certificateService
                        .getCertificatesByLearner(
                                learnerId
                        )
        );
    }

    @GetMapping(
            "/courses/{courseId}/certificates"
    )
    public ResponseEntity<List<CertificateResponseDTO>>
    getCertificatesByCourse(
            @PathVariable UUID courseId
    ) {
        return ResponseEntity.ok(
                certificateService
                        .getCertificatesByCourse(
                                courseId
                        )
        );
    }

    @GetMapping("/certificates")
    public ResponseEntity<List<CertificateResponseDTO>>
    getCertificates(
            @RequestParam(required = false) String status
    ) {
        if (status == null || status.isBlank()) {
            return ResponseEntity.ok(
                    certificateService.getAllCertificates()
            );
        }

        CertificateStatus certificateStatus =
                parseCertificateStatus(status);

        return ResponseEntity.ok(
                certificateService
                        .getCertificatesByStatus(
                                certificateStatus
                        )
        );
    }

    @PatchMapping(
            "/certificates/{certificateId}/revoke"
    )
    public ResponseEntity<CertificateResponseDTO>
    revokeCertificate(
            @PathVariable UUID certificateId,
            @Valid
            @RequestBody
            CertificateRevocationRequestDTO request
    ) {
        return ResponseEntity.ok(
                certificateService.revokeCertificate(
                        certificateId,
                        request
                )
        );
    }

    @GetMapping(
            value = "/certificates/{certificateId}/pdf",
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<byte[]> downloadCertificatePdf(
            @PathVariable UUID certificateId
    ) {
        CertificateResponseDTO certificate =
                certificateService.getCertificateById(
                        certificateId
                );

        byte[] pdfBytes =
                certificateService.generateCertificatePdf(
                        certificateId
                );

        String filename =
                "SkillSphere-Certificate-"
                        + certificate.getCertificateNumber()
                        + ".pdf";

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_PDF
        );

        headers.setContentDisposition(
                ContentDisposition
                        .attachment()
                        .filename(filename)
                        .build()
        );

        headers.setContentLength(
                pdfBytes.length
        );

        return new ResponseEntity<>(
                pdfBytes,
                headers,
                HttpStatus.OK
        );
    }

    @GetMapping(
            "/learners/{learnerId}/certificates/count"
    )
    public ResponseEntity<Map<String, Object>>
    countLearnerCertificates(
            @PathVariable UUID learnerId
    ) {
        long count =
                certificateService
                        .countCertificatesByLearner(
                                learnerId
                        );

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "learnerId",
                learnerId
        );

        response.put(
                "certificateCount",
                count
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping(
            "/courses/{courseId}/certificates/count"
    )
    public ResponseEntity<Map<String, Object>>
    countCourseCertificates(
            @PathVariable UUID courseId,
            @RequestParam(required = false)
            String status
    ) {
        long count;
        String statusValue;

        if (status == null || status.isBlank()) {
            count =
                    certificateService
                            .countCertificatesByCourse(
                                    courseId
                            );

            statusValue = "ALL";
        } else {
            CertificateStatus certificateStatus =
                    parseCertificateStatus(status);

            count =
                    certificateService
                            .countCertificatesByCourseAndStatus(
                                    courseId,
                                    certificateStatus
                            );

            statusValue =
                    certificateStatus.name();
        }

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "courseId",
                courseId
        );

        response.put(
                "status",
                statusValue
        );

        response.put(
                "count",
                count
        );

        return ResponseEntity.ok(response);
    }

    private CertificateStatus parseCertificateStatus(
            String status
    ) {
        try {
            return CertificateStatus.valueOf(
                    status.trim()
                            .toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid certificate status: "
                            + status
                            + ". Allowed values are: "
                            + "ISSUED, REVOKED"
            );
        }
    }
}