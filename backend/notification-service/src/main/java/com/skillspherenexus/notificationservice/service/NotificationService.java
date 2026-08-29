package com.skillspherenexus.notificationservice.service;

import com.skillspherenexus.notificationservice.dto.NotificationCreateRequest;
import com.skillspherenexus.notificationservice.dto.NotificationResponse;
import com.skillspherenexus.notificationservice.entity.Notification;
import com.skillspherenexus.notificationservice.enums.NotificationType;
import com.skillspherenexus.notificationservice.enums.TargetRole;
import com.skillspherenexus.notificationservice.exception.ResourceNotFoundException;
import com.skillspherenexus.notificationservice.kafka.NotificationEventProducer;
import com.skillspherenexus.notificationservice.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository repository;
    private final NotificationEventProducer eventProducer;

    public NotificationResponse create(NotificationCreateRequest request) {

        Notification saved = repository.save(
                Notification.builder()
                        .type(request.type())
                        .title(request.title())
                        .message(request.message())
                        .sourceService("notification-service")
                        .referenceId(request.referenceId())
                        .targetRole(
                                request.targetRole() == null
                                        ? TargetRole.ALL
                                        : request.targetRole()
                        )
                        .targetUserId(request.targetUserId())
                        .isRead(false)
                        .build()
        );

        eventProducer.publish(
                saved.getNotificationId(),
                NotificationResponse.from(saved)
        );

        return NotificationResponse.from(saved);
    }

    public void createFromEvent(
            NotificationType type,
            String sourceService,
            String title,
            String message,
            String referenceId
    ) {

        boolean duplicateRecent =
                repository
                        .findAllByOrderByCreatedAtDesc()
                        .stream()
                        .limit(50)
                        .anyMatch(n ->
                                n.getType() == type
                                && referenceId != null
                                && referenceId.equals(n.getReferenceId())
                                && n.getCreatedAt() != null
                                && n.getCreatedAt()
                                    .isAfter(
                                        LocalDateTime.now()
                                            .minusMinutes(5)
                                    )
                        );

        if (duplicateRecent) {
            return;
        }

        Notification saved = repository.save(
                Notification.builder()
                        .type(type)
                        .title(title)
                        .message(message)
                        .sourceService(sourceService)
                        .referenceId(referenceId)
                        .targetRole(TargetRole.ALL)
                        .isRead(false)
                        .build()
        );

        eventProducer.publish(
                saved.getNotificationId(),
                NotificationResponse.from(saved)
        );
    }

    private List<TargetRole> getVisibleRoles(TargetRole role) {
        if (role == TargetRole.ADMINISTRATOR) {
            return List.of(TargetRole.ADMINISTRATOR, TargetRole.HR_MANAGER, TargetRole.EMPLOYEE, TargetRole.LEARNER, TargetRole.ALL);
        }
        if (role == TargetRole.HR_MANAGER) {
            return List.of(TargetRole.HR_MANAGER, TargetRole.EMPLOYEE, TargetRole.ALL);
        }
        return List.of(role, TargetRole.ALL);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listForRole(
            TargetRole role,
            Boolean read
    ) {

        List<TargetRole> visibleRoles = getVisibleRoles(role);

        List<Notification> rows;

        if (read == null) {

            rows =
                    repository
                            .findByTargetRoleInOrderByCreatedAtDesc(
                                    visibleRoles
                            );

        } else {

            rows =
                    repository
                            .findByTargetRoleInAndIsReadOrderByCreatedAtDesc(
                                    visibleRoles,
                                    read
                            );
        }

        return rows
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public long unreadCountForRole(TargetRole role) {

        return repository
                .findByTargetRoleInAndIsReadOrderByCreatedAtDesc(
                        getVisibleRoles(role),
                        false
                )
                .size();
    }

    public NotificationResponse markRead(UUID notificationId) {

        Notification notification =
                repository
                        .findById(notificationId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Notification not found with ID: "
                                                + notificationId
                                )
                        );

        if (!Boolean.TRUE.equals(notification.getIsRead())) {

            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());

            repository.save(notification);
        }

        return NotificationResponse.from(notification);
    }

    public int markAllReadForRole(TargetRole role) {

        List<Notification> unread =
                repository
                        .findByTargetRoleInAndIsReadOrderByCreatedAtDesc(
                                List.of(role, TargetRole.ALL),
                                false
                        );

        LocalDateTime now = LocalDateTime.now();

        unread.forEach(notification -> {

            notification.setIsRead(true);
            notification.setReadAt(now);

        });

        repository.saveAll(unread);

        return unread.size();
    }
}