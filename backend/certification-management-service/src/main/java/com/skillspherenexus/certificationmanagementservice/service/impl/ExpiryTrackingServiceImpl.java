package com.skillspherenexus.certificationmanagementservice.service.impl;

import com.skillspherenexus.certificationmanagementservice.dto.*;
import com.skillspherenexus.certificationmanagementservice.entity.CertificationRecord;
import com.skillspherenexus.certificationmanagementservice.enums.*;
import com.skillspherenexus.certificationmanagementservice.mapper.CertificationMapper;
import com.skillspherenexus.certificationmanagementservice.repository.CertificationRecordRepository;
import com.skillspherenexus.certificationmanagementservice.security.RequestActor;
import com.skillspherenexus.certificationmanagementservice.service.*;
import com.skillspherenexus.certificationmanagementservice.util.ExpiryPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpiryTrackingServiceImpl implements ExpiryTrackingService {
    private final CertificationRecordRepository repository;
    private final CertificationMapper mapper;
    private final AuditService auditService;
    private final NotificationService notificationService;

    @Value("${certification.expiry.default-warning-days:30}")
    private int defaultWarningDays;

    @Override
    public CertificationRecord evaluate(CertificationRecord record) {
        LocalDate today = LocalDate.now();
        int warningDays = record.getWarningWindowDays() == null ? defaultWarningDays : record.getWarningWindowDays();
        CertificationStatus status = ExpiryPolicy.status(record.getExpiryDate(), today, warningDays, Boolean.TRUE.equals(record.getActive()));
        record.setStatus(status);
        record.setRenewalDueDate(record.getExpiryDate() == null ? null : record.getExpiryDate().minusDays(warningDays));
        RenewalStatus current = record.getRenewalStatus();
        if (!Boolean.TRUE.equals(record.getActive()) || record.getExpiryDate() == null) {
            record.setRenewalStatus(RenewalStatus.NOT_REQUIRED);
        } else if (current == RenewalStatus.PENDING) {
            record.setRenewalStatus(RenewalStatus.PENDING);
        } else if (status == CertificationStatus.EXPIRED) {
            record.setRenewalStatus(RenewalStatus.OVERDUE);
        } else {
            long remaining = Objects.requireNonNullElse(ExpiryPolicy.daysRemaining(record.getExpiryDate(), today), Long.MAX_VALUE);
            if (remaining <= warningDays) record.setRenewalStatus(RenewalStatus.DUE);
            else if (remaining <= 90) record.setRenewalStatus(RenewalStatus.UPCOMING);
            else record.setRenewalStatus(RenewalStatus.NOT_REQUIRED);
        }
        record.setLastEvaluatedAt(LocalDateTime.now());
        return record;
    }

    @Override @Transactional(readOnly = true)
    public List<CertificationResponse> expiringWithin(int days) {
        validateDays(days);
        LocalDate today = LocalDate.now();
        return repository.findByActiveTrueAndExpiryDateBetweenOrderByExpiryDateAsc(today, today.plusDays(days)).stream().map(mapper::toResponse).toList();
    }

    @Override @Transactional(readOnly = true)
    public List<CertificationResponse> expired() {
        return repository.findByActiveTrueAndExpiryDateBeforeOrderByExpiryDateAsc(LocalDate.now()).stream().map(mapper::toResponse).toList();
    }

    @Override
    public BulkEvaluationResponse evaluateAll(RequestActor actor) {
        int processed = 0, changed = 0;
        for (CertificationRecord record : repository.findAll()) {
            CertificationStatus oldStatus = record.getStatus();
            RenewalStatus oldRenewal = record.getRenewalStatus();
            evaluate(record);
            repository.save(record);
            processed++;
            if (oldStatus != record.getStatus() || oldRenewal != record.getRenewalStatus()) {
                changed++;
                auditService.record(record.getCertificationId(), AuditAction.STATUS_EVALUATED, actor,
                        "Certification lifecycle status was recalculated from stored dates", oldStatus + "/" + oldRenewal,
                        record.getStatus() + "/" + record.getRenewalStatus());
            }
        }
        int notifications = notificationService.generateDueNotifications(actor);
        return new BulkEvaluationResponse(processed, changed, notifications, LocalDateTime.now());
    }

    @Override @Transactional(readOnly = true)
    public List<ExpiryBucketResponse> distribution() {
        LocalDate today = LocalDate.now();
        List<CertificationRecord> active = repository.findByActiveTrueOrderByExpiryDateAsc();
        long expired=0, d30=0, d60=0, d90=0, safe=0, noExpiry=0;
        for (CertificationRecord c : active) {
            if (c.getExpiryDate() == null) { noExpiry++; continue; }
            long days = ExpiryPolicy.daysRemaining(c.getExpiryDate(), today);
            if (days < 0) expired++;
            else if (days <= 30) d30++;
            else if (days <= 60) d60++;
            else if (days <= 90) d90++;
            else safe++;
        }
        return List.of(new ExpiryBucketResponse("EXPIRED","Expired",expired),
                new ExpiryBucketResponse("DAYS_0_30","0-30 Days",d30),
                new ExpiryBucketResponse("DAYS_31_60","31-60 Days",d60),
                new ExpiryBucketResponse("DAYS_61_90","61-90 Days",d90),
                new ExpiryBucketResponse("SAFE","Valid Beyond 90 Days",safe),
                new ExpiryBucketResponse("NO_EXPIRY","No Expiry",noExpiry));
    }

    private void validateDays(int days) {
        if (days < 1 || days > 365) throw new IllegalArgumentException("Expiry window must be between 1 and 365 days");
    }
}
