package com.skillspherenexus.careerservice.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingEffectivenessDTO {
    private String courseName;
    private String courseId;
    private Long enrollments;
    private Integer completionRate;
    private Double averageScore;
    private Double scoreImprovement;
}
