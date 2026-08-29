package com.skillspherenexus.learningservice.service;

import com.skillspherenexus.learningservice.dto.CertificateIssueRequestDTO;
import com.skillspherenexus.learningservice.dto.CertificateResponseDTO;
import com.skillspherenexus.learningservice.dto.CertificateRevocationRequestDTO;
import com.skillspherenexus.learningservice.enums.CertificateStatus;

import java.util.List;
import java.util.UUID;

public interface CertificateService {

    CertificateResponseDTO issueCertificate(
            UUID completionId,
            CertificateIssueRequestDTO request
    );

    CertificateResponseDTO getCertificateById(
            UUID certificateId
    );

    CertificateResponseDTO getCertificateByCompletion(
            UUID completionId
    );

    CertificateResponseDTO getCertificateByNumber(
            String certificateNumber
    );

    CertificateResponseDTO verifyCertificate(
            UUID verificationCode
    );

    List<CertificateResponseDTO> getCertificatesByLearner(
            UUID learnerId
    );

    List<CertificateResponseDTO> getCertificatesByCourse(
            UUID courseId
    );

    List<CertificateResponseDTO> getAllCertificates();

    List<CertificateResponseDTO> getCertificatesByStatus(
            CertificateStatus status
    );

    CertificateResponseDTO revokeCertificate(
            UUID certificateId,
            CertificateRevocationRequestDTO request
    );

    byte[] generateCertificatePdf(
            UUID certificateId
    );

    boolean isCertificateValid(
            UUID verificationCode
    );

    long countCertificatesByLearner(
            UUID learnerId
    );

    long countCertificatesByCourse(
            UUID courseId
    );

    long countCertificatesByCourseAndStatus(
            UUID courseId,
            CertificateStatus status
    );
}