package com.skillspherenexus.notificationservice.repository;

import com.skillspherenexus.notificationservice.entity.Notification;
import com.skillspherenexus.notificationservice.enums.TargetRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findAllByOrderByCreatedAtDesc();

    List<Notification> findByIsReadOrderByCreatedAtDesc(Boolean isRead);

    List<Notification> findByTargetRoleInOrderByCreatedAtDesc(
            List<TargetRole> targetRoles
    );

    List<Notification> findByTargetRoleInAndIsReadOrderByCreatedAtDesc(
            List<TargetRole> targetRoles,
            Boolean isRead
    );

    long countByIsReadFalse();
}