package com.skillspherenexus.learningservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HrDashboardResponseDTO {
    private Long totalLearners;
    private Long totalCourses;
    private Long publishedCourses;
    private Long totalEnrollments;
    private Long activeEnrollments;
    private Long completedEnrollments;
    private Long pendingPayments;
    private Long issuedCertificates;
    private Long totalLearningPaths;
    private Long publishedLearningPaths;
    private LocalDateTime generatedAt;
}
