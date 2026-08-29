package com.skillspherenexus.certificationmanagementservice.service.impl;

import com.skillspherenexus.certificationmanagementservice.dto.NotificationResponse;
import com.skillspherenexus.certificationmanagementservice.entity.*;
import com.skillspherenexus.certificationmanagementservice.enums.*;
import com.skillspherenexus.certificationmanagementservice.exception.ResourceNotFoundException;
import com.skillspherenexus.certificationmanagementservice.repository.*;
import com.skillspherenexus.certificationmanagementservice.security.RequestActor;
import com.skillspherenexus.certificationmanagementservice.service.*;
import com.skillspherenexus.certificationmanagementservice.util.ExpiryPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {
    private final RenewalNotificationRepository repository;
    private final CertificationRecordRepository certificationRepository;
    private final AuditService auditService;

    @Override
    public int generateDueNotifications(RequestActor actor) {
        int created = 0; LocalDate today = LocalDate.now();
        for (CertificationRecord cert : certificationRepository.findByActiveTrueOrderByExpiryDateAsc()) {
            if (cert.getExpiryDate() == null) continue;
            long days = ExpiryPolicy.daysRemaining(cert.getExpiryDate(), today);
            if (days < 0) created += create(cert, NotificationType.CERTIFICATION_EXPIRED, "Certification has expired and requires renewal review", actor);
            else if (days <= 7) created += create(cert, NotificationType.EXPIRING_WITHIN_7_DAYS, "Certification expires within 7 days", actor);
            else if (days <= 30) created += create(cert, NotificationType.EXPIRING_WITHIN_30_DAYS, "Certification expires within 30 days", actor);
            if (cert.getRenewalStatus() == RenewalStatus.DUE || cert.getRenewalStatus() == RenewalStatus.OVERDUE) created += create(cert, NotificationType.RENEWAL_DUE, "Certification renewal action is due", actor);
        }
        return created;
    }

    @Override @Transactional(readOnly = true)
    public List<NotificationResponse> list(NotificationStatus status) {
        List<RenewalNotification> rows = status == null ? repository.findAllByOrderBySentAtDesc() : repository.findByStatusOrderBySentAtDesc(status);
        return rows.stream().map(this::map).toList();
    }

    @Override
    public NotificationResponse acknowledge(UUID notificationId, RequestActor actor) {
        RenewalNotification n = repository.findById(notificationId).orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId));
        if (n.getStatus() != NotificationStatus.ACKNOWLEDGED) {
            n.setStatus(NotificationStatus.ACKNOWLEDGED); n.setAcknowledgedAt(LocalDateTime.now()); repository.save(n);
            auditService.record(n.getCertification().getCertificationId(), AuditAction.NOTIFICATION_ACKNOWLEDGED, actor, "Renewal notification acknowledged", null, n.getType());
        }
        return map(n);
    }

    private int create(CertificationRecord cert, NotificationType type, String message, RequestActor actor) {
        String key = cert.getCertificationId() + ":" + type + ":" + cert.getExpiryDate();
        if (repository.existsByDeduplicationKey(key)) return 0;
        RenewalNotification saved = repository.save(RenewalNotification.builder().certification(cert).type(type).status(NotificationStatus.SENT)
                .message(message).dueDate(cert.getExpiryDate()).deduplicationKey(key).sentAt(LocalDateTime.now()).build());
        auditService.record(cert.getCertificationId(), AuditAction.NOTIFICATION_SENT, actor, message, null, saved.getNotificationId());
        return 1;
    }
    private NotificationResponse map(RenewalNotification n) { CertificationRecord c=n.getCertification(); return new NotificationResponse(n.getNotificationId(), c.getCertificationId(), c.getCertificationName(), c.getEmployeeName(), n.getType(), n.getStatus(), n.getMessage(), n.getDueDate(), n.getSentAt(), n.getAcknowledgedAt()); }
}
