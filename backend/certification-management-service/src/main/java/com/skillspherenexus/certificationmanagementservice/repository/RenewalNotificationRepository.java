package com.skillspherenexus.certificationmanagementservice.repository;

import com.skillspherenexus.certificationmanagementservice.entity.RenewalNotification;
import com.skillspherenexus.certificationmanagementservice.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface RenewalNotificationRepository extends JpaRepository<RenewalNotification, UUID> {
    boolean existsByDeduplicationKey(String deduplicationKey);
    List<RenewalNotification> findAllByOrderBySentAtDesc();
    List<RenewalNotification> findByStatusOrderBySentAtDesc(NotificationStatus status);
}
