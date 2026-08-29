package com.skillspherenexus.notificationservice.dto;

import com.skillspherenexus.notificationservice.entity.Notification;
import com.skillspherenexus.notificationservice.enums.NotificationType;
import com.skillspherenexus.notificationservice.enums.TargetRole;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID notificationId,
        NotificationType type,
        String title,
        String message,
        String sourceService,
        String referenceId,
        TargetRole targetRole,
        Boolean isRead,
        LocalDateTime createdAt,
        LocalDateTime readAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getNotificationId(), n.getType(), n.getTitle(), n.getMessage(),
                n.getSourceService(), n.getReferenceId(), n.getTargetRole(),
                n.getIsRead(), n.getCreatedAt(), n.getReadAt());
    }
}
