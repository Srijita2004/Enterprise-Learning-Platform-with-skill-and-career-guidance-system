package com.skillspherenexus.learningservice.repository;

import com.skillspherenexus.learningservice.entity.Certificate;
import com.skillspherenexus.learningservice.enums.CertificateStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CertificateRepository
        extends JpaRepository<Certificate, UUID> {

    Optional<Certificate>
    findByCourseCompletionCompletionId(
            UUID completionId
    );

    boolean existsByCourseCompletionCompletionId(
            UUID completionId
    );

    Optional<Certificate>
    findByCertificateNumberIgnoreCase(
            String certificateNumber
    );

    boolean existsByCertificateNumberIgnoreCase(
            String certificateNumber
    );

    Optional<Certificate>
    findByVerificationCode(
            UUID verificationCode
    );

    List<Certificate>
    findAllByLearnerIdOrderByIssuedAtDesc(
            UUID learnerId
    );

    List<Certificate>
    findAllByCourseIdOrderByIssuedAtDesc(
            UUID courseId
    );

    List<Certificate>
    findAllByStatusOrderByIssuedAtDesc(
            CertificateStatus status
    );

    long countByLearnerId(
            UUID learnerId
    );

    long countByCourseId(
            UUID courseId
    );

    long countByCourseIdAndStatus(
            UUID courseId,
            CertificateStatus status
    );
    List<Certificate> findAllByOrderByIssuedAtDesc();

    long countByStatus(CertificateStatus status);
}