package com.skillspherenexus.skillmanagementservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmployeeSkillRequestDTO {

    @NotNull
    private Integer employeeId;

    @NotNull
    private Integer skillId;

    @NotNull
    private Integer proficiencyLevel;

    @NotNull
    private Integer yearsOfExperience;
}
