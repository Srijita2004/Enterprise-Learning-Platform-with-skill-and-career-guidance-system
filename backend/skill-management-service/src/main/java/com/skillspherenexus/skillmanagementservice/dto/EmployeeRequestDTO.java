package com.skillspherenexus.skillmanagementservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmployeeRequestDTO {

    @NotBlank
    private String employeeName;

    @NotBlank
    private String designation;

    @NotNull
    private Double salary;
}
