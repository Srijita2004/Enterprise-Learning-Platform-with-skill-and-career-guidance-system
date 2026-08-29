package com.skillspherenexus.certificationmanagementservice.repository;

import com.skillspherenexus.certificationmanagementservice.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Optional<AuditLog> findTopByCertificationIdOrderBySequenceNumberDesc(UUID certificationId);
    List<AuditLog> findByCertificationIdOrderBySequenceNumberDesc(UUID certificationId);
    List<AuditLog> findTop200ByOrderBySequenceNumberDesc();
}
