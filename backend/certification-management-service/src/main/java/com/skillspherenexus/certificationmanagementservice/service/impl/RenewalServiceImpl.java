package com.skillspherenexus.certificationmanagementservice.service.impl;

import com.skillspherenexus.certificationmanagementservice.dto.*;
import com.skillspherenexus.certificationmanagementservice.entity.*;
import com.skillspherenexus.certificationmanagementservice.enums.*;
import com.skillspherenexus.certificationmanagementservice.event.CertificateRenewedEvent;
import com.skillspherenexus.certificationmanagementservice.exception.ResourceNotFoundException;
import com.skillspherenexus.certificationmanagementservice.kafka.CertificationEventProducer;
import com.skillspherenexus.certificationmanagementservice.repository.*;
import com.skillspherenexus.certificationmanagementservice.security.RequestActor;
import com.skillspherenexus.certificationmanagementservice.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class RenewalServiceImpl implements RenewalService {
    private final RenewalRequestRepository renewalRepository;
    private final CertificationRecordRepository certificationRepository;
    private final ExpiryTrackingService expiryTrackingService;
    private final AuditService auditService;
    private final CertificationEventProducer certificationEventProducer;

    @Override
    public RenewalRequestResponse request(RenewalCreateRequest request, RequestActor actor) {
        CertificationRecord cert = certificationRepository.findById(request.certificationId()).orElseThrow(() -> new ResourceNotFoundException("Certification not found with ID: " + request.certificationId()));
        if (!Boolean.TRUE.equals(cert.getActive())) throw new IllegalArgumentException("Revoked certifications cannot be renewed");
        if (renewalRepository.existsByCertificationCertificationIdAndStatus(cert.getCertificationId(), RenewalRequestStatus.PENDING)) throw new IllegalArgumentException("A renewal request is already pending for this certification");
        LocalDate comparison = cert.getExpiryDate() == null ? cert.getIssueDate() : cert.getExpiryDate();
        if (!request.proposedExpiryDate().isAfter(comparison)) throw new IllegalArgumentException("Proposed expiry date must be later than the current validity date");
        RenewalRequest entity = RenewalRequest.builder().certification(cert).currentExpiryDate(cert.getExpiryDate())
                .proposedExpiryDate(request.proposedExpiryDate()).justification(request.justification().trim())
                .status(RenewalRequestStatus.PENDING).requestedByUserId(actor.userId()).requestedByRole(actor.role()).requestedAt(LocalDateTime.now()).build();
        cert.setRenewalStatus(RenewalStatus.PENDING); certificationRepository.save(cert);
        RenewalRequest saved = renewalRepository.save(entity);
        auditService.record(cert.getCertificationId(), AuditAction.RENEWAL_REQUESTED, actor, "Renewal request submitted", null, map(saved));
        return map(saved);
    }

    @Override
    public RenewalRequestResponse approve(UUID requestId, RenewalDecisionRequest request, RequestActor actor) {
        RenewalRequest renewal = require(requestId); requirePending(renewal);
        CertificationRecord cert = renewal.getCertification();
        LocalDate oldExpiry = cert.getExpiryDate();
        cert.setExpiryDate(renewal.getProposedExpiryDate()); cert.setRenewalStatus(RenewalStatus.COMPLETED);
        expiryTrackingService.evaluate(cert); cert.setRenewalStatus(RenewalStatus.COMPLETED); certificationRepository.save(cert);
        renewal.setStatus(RenewalRequestStatus.COMPLETED); renewal.setDecisionByUserId(actor.userId()); renewal.setDecisionByRole(actor.role());
        renewal.setDecisionNote(request == null ? null : request.decisionNote()); renewal.setDecidedAt(LocalDateTime.now());
        renewal.setOnTime(oldExpiry == null || !LocalDate.now().isAfter(oldExpiry)); RenewalRequest saved = renewalRepository.save(renewal);
        auditService.record(cert.getCertificationId(), AuditAction.RENEWAL_APPROVED, actor, "Renewal approved and new expiry date applied", oldExpiry, cert.getExpiryDate());
        certificationEventProducer.publishCertificateRenewed(CertificateRenewedEvent.builder()
                .certificationId(cert.getCertificationId())
                .employeeId(cert.getEmployeeId())
                .employeeName(cert.getEmployeeName())
                .certificationName(cert.getCertificationName())
                .oldExpiryDate(oldExpiry)
                .newExpiryDate(cert.getExpiryDate())
                .build());
        return map(saved);
    }

    @Override
    public RenewalRequestResponse reject(UUID requestId, RenewalDecisionRequest request, RequestActor actor) {
        RenewalRequest renewal = require(requestId); requirePending(renewal);
        CertificationRecord cert = renewal.getCertification();
        renewal.setStatus(RenewalRequestStatus.REJECTED); renewal.setDecisionByUserId(actor.userId()); renewal.setDecisionByRole(actor.role());
        renewal.setDecisionNote(request == null ? null : request.decisionNote()); renewal.setDecidedAt(LocalDateTime.now()); renewal.setOnTime(false);
        cert.setRenewalStatus(cert.getExpiryDate() != null && cert.getExpiryDate().isBefore(LocalDate.now()) ? RenewalStatus.OVERDUE : RenewalStatus.REJECTED);
        certificationRepository.save(cert); RenewalRequest saved = renewalRepository.save(renewal);
        auditService.record(cert.getCertificationId(), AuditAction.RENEWAL_REJECTED, actor, "Renewal request rejected", null, map(saved));
        return map(saved);
    }

    @Override @Transactional(readOnly = true)
    public List<RenewalRequestResponse> list() { return renewalRepository.findAllByOrderByRequestedAtDesc().stream().map(this::map).toList(); }

    @Override @Transactional(readOnly = true)
    public double renewalRate() {
        long completed = renewalRepository.countByStatus(RenewalRequestStatus.COMPLETED);
        long rejected = renewalRepository.countByStatus(RenewalRequestStatus.REJECTED);
        long finalized = completed + rejected;
        return finalized == 0 ? 0.0 : Math.round((completed * 10000.0 / finalized)) / 100.0;
    }

    private RenewalRequest require(UUID id) { return renewalRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Renewal request not found with ID: " + id)); }
    private void requirePending(RenewalRequest request) { if (request.getStatus() != RenewalRequestStatus.PENDING) throw new IllegalArgumentException("Only pending renewal requests can be decided"); }
    private RenewalRequestResponse map(RenewalRequest r) { CertificationRecord c = r.getCertification(); return new RenewalRequestResponse(r.getRenewalRequestId(), c.getCertificationId(), c.getCertificationName(), c.getEmployeeId(), c.getEmployeeName(), r.getCurrentExpiryDate(), r.getProposedExpiryDate(), r.getJustification(), r.getStatus(), r.getRequestedByUserId(), r.getRequestedByRole(), r.getRequestedAt(), r.getDecisionByUserId(), r.getDecisionByRole(), r.getDecisionNote(), r.getDecidedAt(), r.getOnTime()); }
}
