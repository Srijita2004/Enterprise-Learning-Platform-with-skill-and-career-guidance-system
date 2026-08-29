package com.skillspherenexus.skillmanagementservice.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmployeeCompetencyRequestDTO {

    @NotNull
    private Integer employeeId;

    @NotNull
    private Integer competencyId;

    @NotNull
    private Integer currentLevel;
}
