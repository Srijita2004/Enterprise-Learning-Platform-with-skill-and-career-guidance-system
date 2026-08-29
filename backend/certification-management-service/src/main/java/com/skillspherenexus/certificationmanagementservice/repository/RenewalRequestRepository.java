package com.skillspherenexus.certificationmanagementservice.repository;

import com.skillspherenexus.certificationmanagementservice.entity.RenewalRequest;
import com.skillspherenexus.certificationmanagementservice.enums.RenewalRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface RenewalRequestRepository extends JpaRepository<RenewalRequest, UUID> {
    List<RenewalRequest> findAllByOrderByRequestedAtDesc();
    List<RenewalRequest> findByCertificationCertificationIdOrderByRequestedAtDesc(UUID certificationId);
    boolean existsByCertificationCertificationIdAndStatus(UUID certificationId, RenewalRequestStatus status);
    long countByStatus(RenewalRequestStatus status);
}
