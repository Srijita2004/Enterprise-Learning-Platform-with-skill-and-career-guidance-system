package com.skillspherenexus.skillmanagementservice.dto;

import lombok.Data;

@Data
public class EmployeeSkillResponseDTO {

    private Integer employeeSkillId;

    private Integer employeeId;

    private Integer skillId;

    private Integer proficiencyLevel;

    private Integer yearsOfExperience;
}
