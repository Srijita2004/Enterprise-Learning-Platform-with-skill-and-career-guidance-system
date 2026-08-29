package com.skillspherenexus.skillmanagementservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SkillRequestDTO {

    @NotBlank
    private String skillName;

    @NotBlank
    private String category;

    private String description;
}
