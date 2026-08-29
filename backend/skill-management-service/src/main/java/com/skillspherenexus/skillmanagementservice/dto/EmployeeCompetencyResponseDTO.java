package com.skillspherenexus.skillmanagementservice.dto;


import lombok.Data;

@Data
public class EmployeeCompetencyResponseDTO {

    private Integer employeeCompetencyId;

    private Integer employeeId;

    private Integer competencyId;

    private Integer currentLevel;
}
