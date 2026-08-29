package com.skillspherenexus.certificationmanagementservice.entity;

import com.skillspherenexus.certificationmanagementservice.enums.NotificationStatus;
import com.skillspherenexus.certificationmanagementservice.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "renewal_notifications", uniqueConstraints = @UniqueConstraint(name = "uk_notification_dedupe", columnNames = "deduplication_key"), indexes = {
        @Index(name = "idx_notification_cert", columnList = "certification_id"),
        @Index(name = "idx_notification_status", columnList = "status")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RenewalNotification {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID notificationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "certification_id", nullable = false)
    private CertificationRecord certification;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationStatus status;

    @Column(nullable = false, length = 700)
    private String message;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "deduplication_key", nullable = false, length = 220)
    private String deduplicationKey;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;
}
