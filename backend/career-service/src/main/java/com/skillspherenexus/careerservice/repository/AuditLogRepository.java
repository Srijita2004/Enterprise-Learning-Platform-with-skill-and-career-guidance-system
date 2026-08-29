package com.skillspherenexus.careerservice.repository;

import com.skillspherenexus.careerservice.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    @Query("SELECT al FROM AuditLog al WHERE al.entityType = :entityType AND al.entityId = :entityId ORDER BY al.createdAt DESC")
    List<AuditLog> findByEntityTypeAndId(@Param("entityType") String entityType, @Param("entityId") Long entityId);
    
    @Query("SELECT al FROM AuditLog al WHERE al.entityType = :entityType AND al.actionType = :actionType ORDER BY al.createdAt DESC")
    List<AuditLog> findByEntityTypeAndAction(@Param("entityType") String entityType, @Param("actionType") String actionType);
    
    @Query("SELECT al FROM AuditLog al WHERE al.performedBy = :userId ORDER BY al.createdAt DESC")
    List<AuditLog> findByPerformedBy(@Param("userId") Long userId);
    
    @Query("SELECT al FROM AuditLog al WHERE al.createdAt >= :startDate AND al.createdAt <= :endDate ORDER BY al.createdAt DESC")
    List<AuditLog> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT al FROM AuditLog al WHERE al.entityType = :entityType ORDER BY al.createdAt DESC")
    List<AuditLog> findByEntityType(@Param("entityType") String entityType);
}
