package com.skillspherenexus.notificationservice.entity;

import com.skillspherenexus.notificationservice.enums.NotificationType;
import com.skillspherenexus.notificationservice.enums.TargetRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notification_read", columnList = "isRead"),
        @Index(name = "idx_notification_target_role", columnList = "targetRole")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID notificationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    /** Which microservice raised the underlying event (skill-management-service, learning-service, ...). */
    private String sourceService;

    /** Free-form id of the entity the notification refers to (employeeId, certificationId, courseId, ...). */
    private String referenceId;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TargetRole targetRole = TargetRole.ALL;

    /** Optional: scope the notification to a single user instead of a whole role. */
    private String targetUserId;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isRead = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime readAt;
}
