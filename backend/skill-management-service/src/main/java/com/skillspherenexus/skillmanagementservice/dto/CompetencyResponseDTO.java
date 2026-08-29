package com.skillspherenexus.skillmanagementservice.dto;

import com.skillspherenexus.skillmanagementservice.entity.CompetencyCategory;
import lombok.Data;


@Data
public class CompetencyResponseDTO {

    private Integer competencyId;

    private String name;

    private CompetencyCategory category;

    private String description;

    private Integer maxLevel;
}
