package com.skillspherenexus.skillmanagementservice.event;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Published to the "employee-created" Kafka topic whenever a new employee
 * is registered in the Skill Management Service (M1).
 */
public class EmployeeCreatedEvent implements Serializable {

    private Integer employeeId;
    private String employeeName;
    private String designation;
    private String sourceService;
    private LocalDateTime occurredAt;

    public EmployeeCreatedEvent() {
    }

    public EmployeeCreatedEvent(Integer employeeId, String employeeName, String designation) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.designation = designation;
        this.sourceService = "skill-management-service";
        this.occurredAt = LocalDateTime.now();
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
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
