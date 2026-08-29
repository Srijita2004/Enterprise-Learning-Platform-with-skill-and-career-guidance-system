package com.skillspherenexus.notificationservice.dto;

import com.skillspherenexus.notificationservice.enums.NotificationType;
import com.skillspherenexus.notificationservice.enums.TargetRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationCreateRequest(
        @NotNull NotificationType type,
        @NotBlank String title,
        @NotBlank String message,
        TargetRole targetRole,
        String targetUserId,
        String referenceId
) {
}
