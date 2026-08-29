package com.skillspherenexus.certificationmanagementservice.service.impl;

import com.skillspherenexus.certificationmanagementservice.dto.*;
import com.skillspherenexus.certificationmanagementservice.entity.CertificationRecord;
import com.skillspherenexus.certificationmanagementservice.enums.*;
import com.skillspherenexus.certificationmanagementservice.mapper.CertificationMapper;
import com.skillspherenexus.certificationmanagementservice.repository.*;
import com.skillspherenexus.certificationmanagementservice.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class ReportServiceImpl implements ReportService {
    private final CertificationRecordRepository repository;
    private final RenewalRequestRepository renewalRepository;
    private final CertificationMapper mapper;
    private final RenewalService renewalService;
    private final ExpiryTrackingService expiryTrackingService;

    @Override public ReportSummaryResponse summary(){
        return new ReportSummaryResponse(repository.count(), repository.countByActiveTrue(), repository.countByActiveTrueAndStatus(CertificationStatus.VALID),
                repository.countByActiveTrueAndStatus(CertificationStatus.EXPIRING_SOON), repository.countByActiveTrueAndStatus(CertificationStatus.EXPIRED),
                repository.countByActiveTrueAndVerificationStatus(VerificationStatus.VERIFIED), repository.countByActiveTrueAndComplianceStatus(ComplianceStatus.COMPLIANT),
                repository.countByActiveTrueAndRenewalStatusIn(List.of(RenewalStatus.DUE,RenewalStatus.PENDING,RenewalStatus.OVERDUE)),
                renewalRepository.countByStatus(RenewalRequestStatus.COMPLETED), renewalRepository.countByStatus(RenewalRequestStatus.REJECTED), renewalService.renewalRate(), LocalDateTime.now());
    }
    @Override public String certificationsCsv(){ return csv(repository.findAll().stream().map(mapper::toResponse).toList()); }
    @Override public String expiringCsv(int days){ return csv(expiryTrackingService.expiringWithin(days)); }
    private String csv(List<CertificationResponse> rows){
        StringBuilder s=new StringBuilder("Certification ID,Employee ID,Employee Name,Certification,Issuing Organization,Credential Number,Issue Date,Expiry Date,Days Remaining,Status,Renewal Status,Verification Status,Compliance Status\n");
        for(CertificationResponse r:rows) s.append(q(r.certificationId())).append(',').append(q(r.employeeId())).append(',').append(q(r.employeeName())).append(',').append(q(r.certificationName())).append(',').append(q(r.issuingOrganization())).append(',').append(q(r.credentialNumber())).append(',').append(q(r.issueDate())).append(',').append(q(r.expiryDate())).append(',').append(q(r.daysRemaining())).append(',').append(q(r.status())).append(',').append(q(r.renewalStatus())).append(',').append(q(r.verificationStatus())).append(',').append(q(r.complianceStatus())).append('\n');
        return s.toString();
    }
    private String q(Object v){ String x=v==null?"":String.valueOf(v); return "\"" + x.replace("\"", "\"\"") + "\""; }
}
