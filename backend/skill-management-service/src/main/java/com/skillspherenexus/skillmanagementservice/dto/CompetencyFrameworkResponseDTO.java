package com.skillspherenexus.skillmanagementservice.dto;


import lombok.Data;

@Data
public class CompetencyFrameworkResponseDTO {

    private Integer frameworkId;

    private String role;

    private Integer competencyId;

    private Integer requiredLevel;
}
