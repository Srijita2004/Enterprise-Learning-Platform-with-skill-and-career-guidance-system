package com.skillspherenexus.careerservice.service.impl;

import com.skillspherenexus.careerservice.entity.AuditLog;
import com.skillspherenexus.careerservice.repository.AuditLogRepository;
import com.skillspherenexus.careerservice.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void logAction(String entityType, Long entityId, String actionType, Long performedBy, 
                          String changes, String remarks, String ipAddress) {
        AuditLog auditLog = AuditLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .actionType(actionType)
                .performedBy(performedBy)
                .changes(changes)
                .remarks(remarks)
                .ipAddress(ipAddress)
                .build();
        
        auditLogRepository.save(auditLog);
    }

    @Override
    public void logCreate(String entityType, Long entityId, Long performedBy, String remarks, String ipAddress) {
        logAction(entityType, entityId, "CREATE", performedBy, null, remarks, ipAddress);
    }

    @Override
    public void logUpdate(String entityType, Long entityId, String changes, Long performedBy, 
                         String remarks, String ipAddress) {
        logAction(entityType, entityId, "UPDATE", performedBy, changes, remarks, ipAddress);
    }

    @Override
    public void logDelete(String entityType, Long entityId, Long performedBy, String remarks, String ipAddress) {
        logAction(entityType, entityId, "DELETE", performedBy, null, remarks, ipAddress);
    }

    @Override
    public void logPromotionCriteriaToggle(Long careerPlanId, Long criteriaId, boolean newStatus, 
                                          Long performedBy, String ipAddress) {
        String changes = "PromotionCriteria[" + criteriaId + "].isMet = " + newStatus;
        logAction("PROMOTION_CRITERIA", criteriaId, "UPDATE", performedBy, changes, 
                 "Career Plan: " + careerPlanId, ipAddress);
    }

    @Override
    public void logMentorAssignment(Long careerPlanId, String mentorId, Long performedBy, String ipAddress) {
        String changes = "Mentor assigned: " + mentorId;
        logAction("CAREER_PLAN", careerPlanId, "UPDATE", performedBy, changes, 
                 "Mentor assignment", ipAddress);
    }
}
