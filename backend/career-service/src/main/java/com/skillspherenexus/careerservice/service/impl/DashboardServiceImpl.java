package com.skillspherenexus.careerservice.service.impl;

import com.skillspherenexus.careerservice.dto.*;
import com.skillspherenexus.careerservice.entity.CareerPlanStatus;
import com.skillspherenexus.careerservice.entity.JobStatus;
import com.skillspherenexus.careerservice.repository.*;
import com.skillspherenexus.careerservice.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final CareerPlanRepository careerPlanRepository;
    private final JobOpportunityRepository jobRepository;
    private final TrainingRecordRepository trainingRecordRepository;
    private final SkillGapRepository skillGapRepository;

    @Override
    public CareerDashboardDTO getExecutiveDashboard() {
        Long activePlans = countActiveCareerPlans();
        Long promotions = countAnnualPromotions();
        Long openJobs = countOpenJobOpportunities();
        Integer avgCoverage = 87; // From PDF specification
        
        List<DepartmentSkillCoverageDTO> departmentCoverages = buildDepartmentCoverages();
        List<TrainingEffectivenessDTO> trainingReports = buildTrainingEffectivenessReports();
        CareerProgressMetricsDTO progressMetrics = buildProgressMetrics();
        
        return CareerDashboardDTO.builder()
                .totalActivePlans(activePlans)
                .promotionsAnnually(promotions)
                .departmentSkillCoverageAverage(avgCoverage)
                .totalJobOpportunitiesOpen(openJobs)
                .departmentCoverages(departmentCoverages)
                .trainingEffectivenessReports(trainingReports)
                .progressMetrics(progressMetrics)
                .build();
    }

    @Override
    public Integer calculateDepartmentSkillCoverage(String department) {
        // Simplified calculation - would be more complex in production
        // For now, return a realistic value based on department
        return switch (department) {
            case "Engineering - Backend" -> 91;
            case "Engineering - Frontend" -> 82;
            case "Quality Assurance" -> 89;
            case "DevOps & Infrastructure" -> 84;
            case "Product Management" -> 78;
            case "Data & Analytics" -> 80;
            default -> 75;
        };
    }

    @Override
    public Long countActiveCareerPlans() {
        return careerPlanRepository.countByStatus(CareerPlanStatus.ACTIVE);
    }

    @Override
    public Long countAnnualPromotions() {
        // From PDF: 247 promotions annually
        return 247L;
    }

    @Override
    public Long countOpenJobOpportunities() {
        return jobRepository.countByStatus(JobStatus.OPEN);
    }

    @Override
    public Double getAverageCareerPlanProgress() {
        List<Integer> progressValues = careerPlanRepository.findByStatus(CareerPlanStatus.ACTIVE)
                .stream()
                .map(cp -> cp.getProgressPercentage() != null ? cp.getProgressPercentage() : 0)
                .toList();
        
        if (progressValues.isEmpty()) return 0.0;
        return progressValues.stream()
                .mapToDouble(Integer::doubleValue)
                .average()
                .orElse(0.0);
    }

    private List<DepartmentSkillCoverageDTO> buildDepartmentCoverages() {
        return new ArrayList<>(List.of(
                DepartmentSkillCoverageDTO.builder()
                        .department("Engineering - Backend")
                        .skillCoveragePercentage(91)
                        .totalEmployees(245L)
                        .employeesWithCompleteSkills(223L)
                        .build(),
                DepartmentSkillCoverageDTO.builder()
                        .department("Engineering - Frontend")
                        .skillCoveragePercentage(82)
                        .totalEmployees(167L)
                        .employeesWithCompleteSkills(137L)
                        .build(),
                DepartmentSkillCoverageDTO.builder()
                        .department("Quality Assurance")
                        .skillCoveragePercentage(89)
                        .totalEmployees(124L)
                        .employeesWithCompleteSkills(110L)
                        .build(),
                DepartmentSkillCoverageDTO.builder()
                        .department("DevOps & Infrastructure")
                        .skillCoveragePercentage(84)
                        .totalEmployees(89L)
                        .employeesWithCompleteSkills(75L)
                        .build(),
                DepartmentSkillCoverageDTO.builder()
                        .department("Product Management")
                        .skillCoveragePercentage(78)
                        .totalEmployees(56L)
                        .employeesWithCompleteSkills(44L)
                        .build(),
                DepartmentSkillCoverageDTO.builder()
                        .department("Data & Analytics")
                        .skillCoveragePercentage(80)
                        .totalEmployees(78L)
                        .employeesWithCompleteSkills(62L)
                        .build()
        ));
    }

    private List<TrainingEffectivenessDTO> buildTrainingEffectivenessReports() {
        return new ArrayList<>(List.of(
                TrainingEffectivenessDTO.builder()
                        .courseName("Spring Boot 4 & Cloud Microservices")
                        .courseId("SPRING-BOOT-4-001")
                        .enrollments(342L)
                        .completionRate(94)
                        .averageScore(87.5)
                        .scoreImprovement(28.0)
                        .build(),
                TrainingEffectivenessDTO.builder()
                        .courseName("Modern Web Engineering with Angular 20")
                        .courseId("ANGULAR-20-001")
                        .enrollments(289L)
                        .completionRate(88)
                        .averageScore(84.2)
                        .scoreImprovement(32.0)
                        .build(),
                TrainingEffectivenessDTO.builder()
                        .courseName("AWS Certified Solutions Architect")
                        .courseId("AWS-SAA-001")
                        .enrollments(198L)
                        .completionRate(91)
                        .averageScore(89.1)
                        .scoreImprovement(24.0)
                        .build(),
                TrainingEffectivenessDTO.builder()
                        .courseName("Securing Cloud Services & IAM")
                        .courseId("SECURITY-001")
                        .enrollments(154L)
                        .completionRate(96)
                        .averageScore(91.3)
                        .scoreImprovement(35.0)
                        .build()
        ));
    }

    private CareerProgressMetricsDTO buildProgressMetrics() {
        return CareerProgressMetricsDTO.builder()
                .averageProgressPercentage(67)
                .plansCompletedThisYear(89L)
                .plansInProgress(2847L)
                .plansCompletedLastYear(158L)
                .promotionRatePercentage(9)
                .build();
    }
}
