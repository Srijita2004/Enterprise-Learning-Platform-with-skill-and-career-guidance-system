package com.skillspherenexus.skillmanagementservice.event;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Published to the "skill-updated" Kafka topic whenever an employee's
 * skill/proficiency record is created or changed in M1.
 */
public class SkillUpdatedEvent implements Serializable {

    private Integer employeeId;
    private Integer skillId;
    private Integer proficiencyLevel;
    private Integer yearsOfExperience;
    private String changeType; // CREATED or UPDATED
    private String sourceService;
    private LocalDateTime occurredAt;

    public SkillUpdatedEvent() {
    }

    public SkillUpdatedEvent(Integer employeeId, Integer skillId, Integer proficiencyLevel,
                              Integer yearsOfExperience, String changeType) {
        this.employeeId = employeeId;
        this.skillId = skillId;
        this.proficiencyLevel = proficiencyLevel;
        this.yearsOfExperience = yearsOfExperience;
        this.changeType = changeType;
        this.sourceService = "skill-management-service";
        this.occurredAt = LocalDateTime.now();
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public Integer getSkillId() {
        return skillId;
    }

    public void setSkillId(Integer skillId) {
        this.skillId = skillId;
    }

    public Integer getProficiencyLevel() {
        return proficiencyLevel;
    }

    public void setProficiencyLevel(Integer proficiencyLevel) {
        this.proficiencyLevel = proficiencyLevel;
    }

    public Integer getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(Integer yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public String getSourceService() {
        return sourceService;
    }

    public void setSourceService(String sourceService) {
        this.sourceService = sourceService;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }
}
