package com.skillspherenexus.skillmanagementservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "employee_competencies")
public class EmployeeCompetency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer employeeCompetencyId;

    @Column(nullable = false)
    private Integer employeeId;

    @ManyToOne
    @JoinColumn(name = "competency_id", nullable = false)
    private Competency competency;

    @Column(nullable = false)
    private Integer currentLevel;

    public EmployeeCompetency() {
    }

    public EmployeeCompetency(Integer employeeCompetencyId, Integer employeeId, Competency competency, Integer currentLevel) {
        this.employeeCompetencyId = employeeCompetencyId;
        this.employeeId = employeeId;
        this.competency = competency;
        this.currentLevel = currentLevel;
    }

    // Getters and Setters
    public Integer getEmployeeCompetencyId() { return employeeCompetencyId; }
    public void setEmployeeCompetencyId(Integer employeeCompetencyId) { this.employeeCompetencyId = employeeCompetencyId; }

    public Integer getEmployeeId() { return employeeId; }
    public void setEmployeeId(Integer employeeId) { this.employeeId = employeeId; }

    public Competency getCompetency() { return competency; }
    public void setCompetency(Competency competency) { this.competency = competency; }

    public Integer getCurrentLevel() { return currentLevel; }
    public void setCurrentLevel(Integer currentLevel) { this.currentLevel = currentLevel; }
}