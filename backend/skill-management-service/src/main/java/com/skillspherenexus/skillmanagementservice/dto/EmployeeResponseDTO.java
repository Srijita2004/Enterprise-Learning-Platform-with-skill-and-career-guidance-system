package com.skillspherenexus.skillmanagementservice.dto;

import lombok.Data;

@Data
public class EmployeeResponseDTO {

    private Integer employeeId;

    private String employeeName;

    private String designation;

    private Double salary;
}
