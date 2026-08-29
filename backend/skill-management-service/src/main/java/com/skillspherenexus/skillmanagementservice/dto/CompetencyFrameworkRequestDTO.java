package com.skillspherenexus.skillmanagementservice.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CompetencyFrameworkRequestDTO {

    @NotBlank
    private String role;

    @NotNull
    private Integer competencyId;

    @NotNull
    private Integer requiredLevel;
}
