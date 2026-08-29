package com.skillspherenexus.learningservice.dto;

import com.skillspherenexus.learningservice.enums.CertificateStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateResponseDTO {

    private UUID certificateId;

    private UUID completionId;

    private UUID enrollmentId;

    private UUID learnerId;

    private UUID courseId;

    private String courseCode;

    private String courseTitle;

    private String recipientName;

    private String certificateNumber;

    private UUID verificationCode;

    private CertificateStatus status;

    private Boolean valid;

    private UUID issuedByUserId;

    private UUID revokedByUserId;

    private String fileUrl;

    private LocalDateTime courseCompletedAt;

    private LocalDateTime issuedAt;

    private LocalDateTime revokedAt;

    private String revocationReason;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}