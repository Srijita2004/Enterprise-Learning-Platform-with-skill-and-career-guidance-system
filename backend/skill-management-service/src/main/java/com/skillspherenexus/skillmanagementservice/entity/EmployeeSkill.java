package com.skillspherenexus.skillmanagementservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "employee_skill")
public class EmployeeSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer employeeSkillId;

    private Integer employeeId;

    private Integer skillId;

    private Integer proficiencyLevel;

    private Integer yearsOfExperience;

    public EmployeeSkill() {
    }

    public EmployeeSkill(Integer employeeSkillId, Integer employeeId,
                         Integer skillId, Integer proficiencyLevel,
                         Integer yearsOfExperience) {
        this.employeeSkillId = employeeSkillId;
        this.employeeId = employeeId;
        this.skillId = skillId;
        this.proficiencyLevel = proficiencyLevel;
        this.yearsOfExperience = yearsOfExperience;
    }

    public Integer getEmployeeSkillId() {
        return employeeSkillId;
    }

    public void setEmployeeSkillId(Integer employeeSkillId) {
        this.employeeSkillId = employeeSkillId;
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
}