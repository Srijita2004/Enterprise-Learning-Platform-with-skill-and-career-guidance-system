package com.skillspherenexus.certificationmanagementservice.service.impl;

import com.skillspherenexus.certificationmanagementservice.dto.*;
import com.skillspherenexus.certificationmanagementservice.enums.*;
import com.skillspherenexus.certificationmanagementservice.repository.*;
import com.skillspherenexus.certificationmanagementservice.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class DashboardServiceImpl implements DashboardService {
    private final CertificationRecordRepository repository;
    private final RenewalRequestRepository renewalRepository;
    private final ExpiryTrackingService expiryTrackingService;
    private final RenewalService renewalService;

    @Override public DashboardResponse dashboard(){
        long active=repository.countByActiveTrue();
        return new DashboardResponse(repository.count(), active, repository.countByActiveTrueAndStatus(CertificationStatus.VALID),
                expiryTrackingService.expiringWithin(30).size(), repository.countByActiveTrueAndStatus(CertificationStatus.EXPIRED),
                repository.countByActiveTrueAndRenewalStatusIn(List.of(RenewalStatus.DUE,RenewalStatus.PENDING,RenewalStatus.OVERDUE)),
                renewalRepository.countByStatus(RenewalRequestStatus.PENDING), repository.countByActiveTrueAndVerificationStatus(VerificationStatus.VERIFIED),
                repository.countByActiveTrueAndComplianceStatus(ComplianceStatus.COMPLIANT), renewalService.renewalRate(),
                expiryTrackingService.distribution(), expiryTrackingService.expiringWithin(30).stream().limit(8).toList());
    }
}
