package com.skillspherenexus.certificationmanagementservice.service.impl;

import com.skillspherenexus.certificationmanagementservice.dto.*;
import com.skillspherenexus.certificationmanagementservice.entity.CertificationRecord;
import com.skillspherenexus.certificationmanagementservice.enums.*;
import com.skillspherenexus.certificationmanagementservice.event.CertificateIssuedEvent;
import com.skillspherenexus.certificationmanagementservice.exception.*;
import com.skillspherenexus.certificationmanagementservice.integration.*;
import com.skillspherenexus.certificationmanagementservice.kafka.CertificationEventProducer;
import com.skillspherenexus.certificationmanagementservice.mapper.CertificationMapper;
import com.skillspherenexus.certificationmanagementservice.repository.CertificationRecordRepository;
import com.skillspherenexus.certificationmanagementservice.security.RequestActor;
import com.skillspherenexus.certificationmanagementservice.service.*;
import com.skillspherenexus.certificationmanagementservice.util.ExpiryPolicy;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class CertificationServiceImpl implements CertificationService {
    private final CertificationRecordRepository repository;
    private final CertificationMapper mapper;
    private final M1IntegrationClient m1Client;
    private final ExpiryTrackingService expiryTrackingService;
    private final AuditService auditService;
    private final CertificationEventProducer certificationEventProducer;

    @Value("${certification.expiry.default-warning-days:30}") private int defaultWarningDays;
    @Value("${certification.expiry.maximum-warning-days:365}") private int maximumWarningDays;

    @Override
    public CertificationResponse register(CertificationCreateRequest request, RequestActor actor) {
        validateDates(request.issueDate(), request.expiryDate());
        validateCredential(request.credentialNumber(), null);
        int warning = normalizeWarning(request.warningWindowDays());
        LegacyEmployee employee = m1Client.getEmployee(request.employeeId());
        CertificationRecord record = CertificationRecord.builder()
                .employeeId(employee.employeeId()).employeeName(employee.employeeName())
                .certificationName(clean(request.certificationName())).issuingOrganization(clean(request.issuingOrganization()))
                .credentialNumber(blankToNull(request.credentialNumber())).issueDate(request.issueDate()).expiryDate(request.expiryDate())
                .status(CertificationStatus.VALID).renewalStatus(RenewalStatus.NOT_REQUIRED)
                .verificationStatus(VerificationStatus.PENDING).complianceStatus(ComplianceStatus.PENDING)
                .active(true).warningWindowDays(warning).sourceSystem("M3").createdByUserId(actor.userId()).createdByRole(actor.role()).build();
        expiryTrackingService.evaluate(record);
        CertificationRecord saved = repository.save(record);
        auditService.record(saved.getCertificationId(), AuditAction.REGISTERED, actor, "Certification registered", null, mapper.toResponse(saved));
        certificationEventProducer.publishCertificateIssued(CertificateIssuedEvent.builder()
                .certificationId(saved.getCertificationId())
                .employeeId(saved.getEmployeeId())
                .employeeName(saved.getEmployeeName())
                .certificationName(saved.getCertificationName())
                .expiryDate(saved.getExpiryDate())
                .build());
        return mapper.toResponse(saved);
    }

    @Override
    public CertificationResponse update(UUID id, CertificationUpdateRequest request, RequestActor actor) {
        CertificationRecord record = require(id);
        CertificationResponse before = mapper.toResponse(record);
        validateDates(request.issueDate(), request.expiryDate());
        validateCredential(request.credentialNumber(), id);
        record.setCertificationName(clean(request.certificationName()));
        record.setIssuingOrganization(clean(request.issuingOrganization()));
        record.setCredentialNumber(blankToNull(request.credentialNumber()));
        record.setIssueDate(request.issueDate()); record.setExpiryDate(request.expiryDate());
        record.setWarningWindowDays(normalizeWarning(request.warningWindowDays()));
        expiryTrackingService.evaluate(record);
        CertificationRecord saved = repository.save(record);
        auditService.record(id, AuditAction.UPDATED, actor, "Certification record updated", before, mapper.toResponse(saved));
        return mapper.toResponse(saved);
    }

    @Override @Transactional(readOnly = true)
    public CertificationResponse get(UUID id) { return mapper.toResponse(require(id)); }

    @Override @Transactional(readOnly = true)
    public PagedResponse<CertificationResponse> search(String query, CertificationStatus status, VerificationStatus verification,
                                                       ComplianceStatus compliance, String expiryBucket, int page, int size, String sort) {
        if (page < 0) throw new IllegalArgumentException("Page must be zero or greater");
        if (size < 1 || size > 100) throw new IllegalArgumentException("Page size must be between 1 and 100");
        Sort order = parseSort(sort);
        Specification<CertificationRecord> spec = (root, q, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query != null && !query.isBlank()) {
                String like = "%" + query.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(cb.like(cb.lower(root.get("certificationName")), like), cb.like(cb.lower(root.get("employeeName")), like),
                        cb.like(cb.lower(root.get("issuingOrganization")), like), cb.like(cb.lower(root.get("credentialNumber")), like)));
            }
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (verification != null) predicates.add(cb.equal(root.get("verificationStatus"), verification));
            if (compliance != null) predicates.add(cb.equal(root.get("complianceStatus"), compliance));
            if (expiryBucket != null && !expiryBucket.isBlank()) addExpiryBucket(expiryBucket, predicates, root, cb);
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        Page<CertificationResponse> mapped = repository.findAll(spec, PageRequest.of(page, size, order)).map(mapper::toResponse);
        return PagedResponse.from(mapped);
    }

    @Override
    public CertificationResponse revoke(UUID id, String reason, RequestActor actor) {
        CertificationRecord record = require(id);
        if (!Boolean.TRUE.equals(record.getActive())) throw new IllegalArgumentException("Certification is already revoked");
        CertificationResponse before = mapper.toResponse(record);
        record.setActive(false); record.setStatus(CertificationStatus.REVOKED); record.setRenewalStatus(RenewalStatus.NOT_REQUIRED);
        CertificationRecord saved = repository.save(record);
        auditService.record(id, AuditAction.REVOKED, actor, "Certification revoked: " + reason, before, mapper.toResponse(saved));
        return mapper.toResponse(saved);
    }

    @Override
    public CertificationResponse updateVerification(UUID id, VerificationStatus status, RequestActor actor) {
        CertificationRecord record = require(id);
        VerificationStatus before = record.getVerificationStatus();
        record.setVerificationStatus(status);
        CertificationRecord saved = repository.save(record);
        auditService.record(id, AuditAction.VERIFICATION_UPDATED, actor, "Verification status changed", before, status);
        return mapper.toResponse(saved);
    }

    @Override
    public LegacySyncResponse syncFromM1(RequestActor actor) {
        List<LegacyCertificate> legacy = m1Client.getCertificates();
        int imported=0, skipped=0, failed=0;
        for (LegacyCertificate source : legacy) {
            if (source.certid() == null) { failed++; continue; }
            if (repository.findByLegacyCertificateId(source.certid()).isPresent()) { skipped++; continue; }
            try {
                if (source.empid() == null || source.issueDate() == null || source.name() == null || source.name().isBlank()
                        || source.issuingOrganization() == null || source.issuingOrganization().isBlank()) {
                    failed++;
                    continue;
                }
                LegacyEmployee employee = m1Client.getEmployee(source.empid());
                CertificationRecord record = CertificationRecord.builder()
                        .employeeId(source.empid()).employeeName(employee.employeeName()).certificationName(clean(source.name()))
                        .issuingOrganization(source.issuingOrganization().trim())
                        .credentialNumber(null).issueDate(source.issueDate()).expiryDate(source.expiry()).status(CertificationStatus.VALID)
                        .renewalStatus(RenewalStatus.NOT_REQUIRED).verificationStatus(VerificationStatus.PENDING)
                        .complianceStatus(ComplianceStatus.PENDING).active(true).warningWindowDays(defaultWarningDays)
                        .legacyCertificateId(source.certid()).sourceSystem("M1_SKILL_MANAGEMENT").createdByUserId(actor.userId()).createdByRole(actor.role()).build();
                expiryTrackingService.evaluate(record);
                CertificationRecord saved = repository.save(record);
                auditService.record(saved.getCertificationId(), AuditAction.IMPORTED_FROM_M1, actor,
                        "Certification imported from locked Milestone 1 certificate ID " + source.certid(), null, mapper.toResponse(saved));
                imported++;
            } catch (RuntimeException ex) { failed++; }
        }
        return new LegacySyncResponse(legacy.size(), imported, skipped, failed, LocalDateTime.now());
    }

    private CertificationRecord require(UUID id) { return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Certification not found with ID: " + id)); }
    private int normalizeWarning(Integer days) { int value = days == null ? defaultWarningDays : days; if (value < 1 || value > maximumWarningDays) throw new IllegalArgumentException("Warning window must be between 1 and " + maximumWarningDays + " days"); return value; }
    private void validateDates(LocalDate issue, LocalDate expiry) { if (expiry != null && expiry.isBefore(issue)) throw new IllegalArgumentException("Expiry date cannot be before issue date"); }
    private void validateCredential(String credential, UUID currentId) {
        String normalized = blankToNull(credential); if (normalized == null) return;
        repository.findByCredentialNumberIgnoreCase(normalized).filter(c -> currentId == null || !c.getCertificationId().equals(currentId))
                .ifPresent(c -> { throw new DuplicateResourceException("Credential number is already registered"); });
    }
    private String clean(String value) { return value == null ? "" : value.trim().replaceAll("\\s+", " "); }
    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private Sort parseSort(String value) {
        String raw = value == null || value.isBlank() ? "expiryDate,asc" : value;
        String[] parts = raw.split(","); String property = Set.of("expiryDate","issueDate","certificationName","employeeName","status","updatedAt").contains(parts[0]) ? parts[0] : "expiryDate";
        Sort.Direction direction = parts.length > 1 && parts[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, property);
    }
    private void addExpiryBucket(String bucket, List<Predicate> predicates, jakarta.persistence.criteria.Root<CertificationRecord> root, jakarta.persistence.criteria.CriteriaBuilder cb) {
        LocalDate today = LocalDate.now(); String key = bucket.trim().toUpperCase(Locale.ROOT);
        switch (key) {
            case "EXPIRED" -> predicates.add(cb.lessThan(root.get("expiryDate"), today));
            case "DAYS_0_30" -> predicates.add(cb.between(root.get("expiryDate"), today, today.plusDays(30)));
            case "DAYS_31_60" -> predicates.add(cb.between(root.get("expiryDate"), today.plusDays(31), today.plusDays(60)));
            case "DAYS_61_90" -> predicates.add(cb.between(root.get("expiryDate"), today.plusDays(61), today.plusDays(90)));
            case "SAFE" -> predicates.add(cb.greaterThan(root.get("expiryDate"), today.plusDays(90)));
            case "NO_EXPIRY" -> predicates.add(cb.isNull(root.get("expiryDate")));
            default -> throw new IllegalArgumentException("Unknown expiry bucket: " + bucket);
        }
    }
}
