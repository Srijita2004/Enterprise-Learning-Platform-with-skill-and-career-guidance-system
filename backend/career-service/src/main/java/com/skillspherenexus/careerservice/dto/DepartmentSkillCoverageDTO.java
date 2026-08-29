package com.skillspherenexus.careerservice.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentSkillCoverageDTO {
    private String department;
    private Integer skillCoveragePercentage;
    private Long totalEmployees;
    private Long employeesWithCompleteSkills;
}
