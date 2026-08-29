package com.skillspherenexus.careerservice.service;

public interface AuditLogService {
    
    void logAction(String entityType, Long entityId, String actionType, Long performedBy, String changes, String remarks, String ipAddress);
    
    void logCreate(String entityType, Long entityId, Long performedBy, String remarks, String ipAddress);
    
    void logUpdate(String entityType, Long entityId, String changes, Long performedBy, String remarks, String ipAddress);
    
    void logDelete(String entityType, Long entityId, Long performedBy, String remarks, String ipAddress);
    
    void logPromotionCriteriaToggle(Long careerPlanId, Long criteriaId, boolean newStatus, Long performedBy, String ipAddress);
    
    void logMentorAssignment(Long careerPlanId, String mentorId, Long performedBy, String ipAddress);
}
