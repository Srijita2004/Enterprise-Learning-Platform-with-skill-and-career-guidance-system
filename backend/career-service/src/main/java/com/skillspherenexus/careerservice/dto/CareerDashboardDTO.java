package com.skillspherenexus.careerservice.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CareerDashboardDTO {
    private Long totalActivePlans;
    private Long promotionsAnnually;
    private Integer departmentSkillCoverageAverage;
    private Long totalJobOpportunitiesOpen;
    private List<DepartmentSkillCoverageDTO> departmentCoverages;
    private List<TrainingEffectivenessDTO> trainingEffectivenessReports;
    private CareerProgressMetricsDTO progressMetrics;
}
