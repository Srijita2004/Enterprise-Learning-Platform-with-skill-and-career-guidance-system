package com.skillspherenexus.certificationmanagementservice.dto;

import com.skillspherenexus.certificationmanagementservice.enums.*;
import java.time.*;
import java.util.UUID;

public record NotificationResponse(UUID notificationId, UUID certificationId, String certificationName, String employeeName,
        NotificationType type, NotificationStatus status, String message, LocalDate dueDate, LocalDateTime sentAt, LocalDateTime acknowledgedAt) {}
