package com.skillspherenexus.certificationmanagementservice.repository;

import com.skillspherenexus.certificationmanagementservice.entity.CertificationRecord;
import com.skillspherenexus.certificationmanagementservice.enums.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.*;

public interface CertificationRecordRepository extends JpaRepository<CertificationRecord, UUID>, JpaSpecificationExecutor<CertificationRecord> {
    Optional<CertificationRecord> findByCredentialNumberIgnoreCase(String credentialNumber);
    Optional<CertificationRecord> findByLegacyCertificateId(Integer legacyCertificateId);
    List<CertificationRecord> findByActiveTrueOrderByExpiryDateAsc();
    List<CertificationRecord> findByActiveTrueAndExpiryDateBetweenOrderByExpiryDateAsc(LocalDate start, LocalDate end);
    List<CertificationRecord> findByActiveTrueAndExpiryDateBeforeOrderByExpiryDateAsc(LocalDate date);
    List<CertificationRecord> findByActiveTrueAndStatusOrderByExpiryDateAsc(CertificationStatus status);
    long countByActiveTrue();
    long countByActiveTrueAndStatus(CertificationStatus status);
    long countByActiveTrueAndVerificationStatus(VerificationStatus status);
    long countByActiveTrueAndComplianceStatus(ComplianceStatus status);
    long countByActiveTrueAndRenewalStatusIn(Collection<RenewalStatus> statuses);
}
