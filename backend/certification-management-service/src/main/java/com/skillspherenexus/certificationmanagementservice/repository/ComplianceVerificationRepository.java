package com.skillspherenexus.certificationmanagementservice.repository;

import com.skillspherenexus.certificationmanagementservice.entity.ComplianceVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ComplianceVerificationRepository extends JpaRepository<ComplianceVerification, UUID> {
    List<ComplianceVerification> findByCertificationCertificationIdOrderByVerifiedAtDesc(UUID certificationId);
    List<ComplianceVerification> findAllByOrderByVerifiedAtDesc();
}
