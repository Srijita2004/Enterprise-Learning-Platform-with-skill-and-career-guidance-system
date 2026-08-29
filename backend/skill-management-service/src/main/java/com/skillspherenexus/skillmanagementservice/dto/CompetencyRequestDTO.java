package com.skillspherenexus.skillmanagementservice.dto;

import com.skillspherenexus.skillmanagementservice.entity.CompetencyCategory;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CompetencyRequestDTO {

    @NotBlank
    private String name;

    @NotNull
    private CompetencyCategory category;

    private String description;

    @Min(1)
    @Max(10)
    private Integer maxLevel;
}
