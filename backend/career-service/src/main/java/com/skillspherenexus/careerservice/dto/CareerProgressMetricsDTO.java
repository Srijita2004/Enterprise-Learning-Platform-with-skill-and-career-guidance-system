package com.skillspherenexus.careerservice.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CareerProgressMetricsDTO {
    private Integer averageProgressPercentage;
    private Long plansCompletedThisYear;
    private Long plansInProgress;
    private Long plansCompletedLastYear;
    private Integer promotionRatePercentage;
}
