package com.skillspherenexus.skillmanagementservice.dto;

import lombok.Data;

@Data
public class AssessmentResponseDTO {

    private Long assessmentId;

    private Long employeeId;

    private Long skillId;

    private Integer score;

    private Boolean verified;

    
}