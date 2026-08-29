package com.skillspherenexus.certificationmanagementservice.service.impl;

import com.skillspherenexus.certificationmanagementservice.dto.*;
import com.skillspherenexus.certificationmanagementservice.entity.*;
import com.skillspherenexus.certificationmanagementservice.enums.*;
import com.skillspherenexus.certificationmanagementservice.exception.ResourceNotFoundException;
import com.skillspherenexus.certificationmanagementservice.repository.*;
import com.skillspherenexus.certificationmanagementservice.security.RequestActor;
import com.skillspherenexus.certificationmanagementservice.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service @RequiredArgsConstructor @Transactional
public class ComplianceServiceImpl implements ComplianceService {
    private final ComplianceVerificationRepository repository;
    private final CertificationRecordRepository certificationRepository;
    private final AuditService auditService;

    @Override
    public ComplianceVerificationResponse verify(UUID certificationId, ComplianceVerifyRequest request, RequestActor actor) {
        if (request.result() == ComplianceStatus.PENDING) throw new IllegalArgumentException("Compliance verification result must be COMPLIANT or NON_COMPLIANT");
        CertificationRecord cert = certificationRepository.findById(certificationId).orElseThrow(() -> new ResourceNotFoundException("Certification not found with ID: " + certificationId));
        ComplianceStatus before = cert.getComplianceStatus(); cert.setComplianceStatus(request.result()); certificationRepository.save(cert);
        ComplianceVerification saved = repository.save(ComplianceVerification.builder().certification(cert).result(request.result())
                .policyReference(request.policyReference().trim()).evidenceReference(clean(request.evidenceReference())).notes(clean(request.notes()))
                .verifiedByUserId(actor.userId()).verifiedByRole(actor.role()).verifiedAt(LocalDateTime.now()).build());
        auditService.record(certificationId, AuditAction.COMPLIANCE_VERIFIED, actor, "Compliance verification completed against policy " + request.policyReference(), before, request.result());
        return map(saved);
    }
    @Override @Transactional(readOnly=true) public List<ComplianceVerificationResponse> history(UUID certificationId) { return repository.findByCertificationCertificationIdOrderByVerifiedAtDesc(certificationId).stream().map(this::map).toList(); }
    @Override @Transactional(readOnly=true) public List<ComplianceVerificationResponse> recent() { return repository.findAllByOrderByVerifiedAtDesc().stream().map(this::map).toList(); }
    private String clean(String s){ return s==null||s.isBlank()?null:s.trim(); }
    private ComplianceVerificationResponse map(ComplianceVerification v){ CertificationRecord c=v.getCertification(); return new ComplianceVerificationResponse(v.getComplianceVerificationId(), c.getCertificationId(), c.getCertificationName(), c.getEmployeeId(), c.getEmployeeName(), v.getResult(), v.getPolicyReference(), v.getEvidenceReference(), v.getNotes(), v.getVerifiedByUserId(), v.getVerifiedByRole(), v.getVerifiedAt()); }
}
