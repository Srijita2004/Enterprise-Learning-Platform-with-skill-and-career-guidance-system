package com.skillspherenexus.skillmanagementservice.dto;

import lombok.Data;

@Data
public class SkillResponseDTO {

    private Integer skillId;

    private String skillName;

    private String category;

    private String description;
}
