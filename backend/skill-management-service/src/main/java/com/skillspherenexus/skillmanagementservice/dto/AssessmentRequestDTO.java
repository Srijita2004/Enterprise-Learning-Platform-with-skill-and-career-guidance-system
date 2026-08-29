package com.skillspherenexus.skillmanagementservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssessmentRequestDTO {

    @NotNull
    private Long employeeId;

    @NotNull
    private Long skillId;

    @NotNull
    @Min(0)
    @Max(100)
    private Integer score;

    private Boolean verified;
    
}