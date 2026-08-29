package com.skillspherenexus.careerservice.config;

import com.skillspherenexus.careerservice.entity.*;
import com.skillspherenexus.careerservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CareerPlanRepository careerPlanRepository;
    private final PromotionCriteriaRepository promotionCriteriaRepository;
    private final JobOpportunityRepository jobOpportunityRepository;
    private final SkillGapRepository skillGapRepository;

    @Override
    public void run(String... args) {
        // 1. Seed Career Plan for Srijita (1)
        if (!careerPlanRepository.findByEmployeeId(1L).isPresent()) {
            CareerPlan srijitaPlan = careerPlanRepository.save(CareerPlan.builder()
                    .employeeId(1L)
                    .employeeName("Srijita")
                    .currentRole("Java Developer")
                    .targetRole("Senior Backend Architect")
                    .progressPercentage(78)
                    .mentorId("107")
                    .mentorName("Jane Doe")
                    .status(CareerPlanStatus.ACTIVE)
                    .description("Targeting Senior Backend Architect transition. Excelling in Java 17 and Spring Boot 4.")
                    .startDate(LocalDateTime.now().minusMonths(8))
                    .targetCompletionDate(LocalDateTime.now().plusMonths(4))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());

            promotionCriteriaRepository.saveAll(List.of(
                    PromotionCriteria.builder()
                            .careerPlan(srijitaPlan)
                            .name("Microservices Architecture")
                            .description("Master enterprise microservices and Kafka event streams")
                            .isMet(true)
                            .metDate(LocalDateTime.now().minusMonths(2))
                            .type(PromotionCriteriaType.SKILL)
                            .sequenceOrder(1)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build(),
                    PromotionCriteria.builder()
                            .careerPlan(srijitaPlan)
                            .name("Cloud Architecture Assessment")
                            .description("Pass cloud architecture & resilience assessment")
                            .isMet(true)
                            .metDate(LocalDateTime.now().minusMonths(1))
                            .type(PromotionCriteriaType.ASSESSMENT)
                            .sequenceOrder(2)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build(),
                    PromotionCriteria.builder()
                            .careerPlan(srijitaPlan)
                            .name("Tenure in Role (>18 months)")
                            .description("Serve at least 18 months as core Java developer")
                            .isMet(true)
                            .metDate(LocalDateTime.now().minusMonths(3))
                            .type(PromotionCriteriaType.TENURE)
                            .sequenceOrder(3)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build()
            ));

            skillGapRepository.saveAll(List.of(
                    SkillGap.builder()
                            .careerPlan(srijitaPlan)
                            .skillName("Cloud Architecture")
                            .currentLevel(3)
                            .requiredLevel(5)
                            .gapLevel(2)
                            .trainingPlan("Spring Boot 4 & Cloud Microservices Architecture")
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build()
            ));
        }

        // 2. Seed Career Plan for John Smith (106)
        if (!careerPlanRepository.findByEmployeeId(106L).isPresent()) {
            CareerPlan johnPlan = careerPlanRepository.save(CareerPlan.builder()
                    .employeeId(106L)
                    .employeeName("John Smith")
                    .currentRole("Developer")
                    .targetRole("Tech Lead")
                    .progressPercentage(67)
                    .mentorId("107")
                    .mentorName("Jane Doe")
                    .status(CareerPlanStatus.ACTIVE)
                    .description("Targeting Technical Lead transition by Q4. Gaps identified in Angular (+3) and System Design (+2).")
                    .startDate(LocalDateTime.now().minusMonths(6))
                    .targetCompletionDate(LocalDateTime.now().plusMonths(6))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());

            promotionCriteriaRepository.saveAll(List.of(
                    PromotionCriteria.builder()
                            .careerPlan(johnPlan)
                            .name("Angular Mastery")
                            .description("Acquire Angular skills (+3 levels)")
                            .isMet(false)
                            .type(PromotionCriteriaType.SKILL)
                            .sequenceOrder(1)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build(),
                    PromotionCriteria.builder()
                            .careerPlan(johnPlan)
                            .name("System Architecture")
                            .description("Complete enterprise distributed system design assessment")
                            .isMet(false)
                            .type(PromotionCriteriaType.ASSESSMENT)
                            .sequenceOrder(2)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build(),
                    PromotionCriteria.builder()
                            .careerPlan(johnPlan)
                            .name("Tenure in Role")
                            .description("Serve at least 18 months as core developer")
                            .isMet(true)
                            .metDate(LocalDateTime.now().minusMonths(1))
                            .type(PromotionCriteriaType.TENURE)
                            .sequenceOrder(3)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build()
            ));

            skillGapRepository.saveAll(List.of(
                    SkillGap.builder()
                            .careerPlan(johnPlan)
                            .skillName("Angular")
                            .currentLevel(2)
                            .requiredLevel(5)
                            .gapLevel(3)
                            .trainingPlan("Modern Web Engineering with Angular 20")
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build(),
                    SkillGap.builder()
                            .careerPlan(johnPlan)
                            .skillName("System Design")
                            .currentLevel(3)
                            .requiredLevel(5)
                            .gapLevel(2)
                            .trainingPlan("Enterprise System Design Patterns")
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build()
            ));
        }

        // 3. Seed Career Plan for Alex Vance (101)
        if (!careerPlanRepository.findByEmployeeId(101L).isPresent()) {
            CareerPlan alexPlan = careerPlanRepository.save(CareerPlan.builder()
                    .employeeId(101L)
                    .employeeName("Alex Vance")
                    .currentRole("Cloud Engineer")
                    .targetRole("Principal Cloud Architect")
                    .progressPercentage(85)
                    .mentorId("102")
                    .mentorName("Marcus Brodie")
                    .status(CareerPlanStatus.ACTIVE)
                    .description("Specializing in multi-cloud Kubernetes orchestration and zero-trust security.")
                    .startDate(LocalDateTime.now().minusMonths(12))
                    .targetCompletionDate(LocalDateTime.now().plusMonths(2))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());

            promotionCriteriaRepository.saveAll(List.of(
                    PromotionCriteria.builder()
                            .careerPlan(alexPlan)
                            .name("Kubernetes CKA")
                            .description("Acquire Certified Kubernetes Administrator")
                            .isMet(true)
                            .metDate(LocalDateTime.now().minusMonths(4))
                            .type(PromotionCriteriaType.CERTIFICATION)
                            .sequenceOrder(1)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build(),
                    PromotionCriteria.builder()
                            .careerPlan(alexPlan)
                            .name("Multi-Cloud Architecture")
                            .description("Complete Multi-Cloud Architecture Assessment")
                            .isMet(true)
                            .metDate(LocalDateTime.now().minusMonths(2))
                            .type(PromotionCriteriaType.ASSESSMENT)
                            .sequenceOrder(2)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build()
            ));
        }

        // 4. Seed Career Plan for Sarah Jenkins (103)
        if (!careerPlanRepository.findByEmployeeId(103L).isPresent()) {
            CareerPlan sarahPlan = careerPlanRepository.save(CareerPlan.builder()
                    .employeeId(103L)
                    .employeeName("Sarah Jenkins")
                    .currentRole("QA Lead")
                    .targetRole("Quality Assurance Director")
                    .progressPercentage(72)
                    .mentorId("104")
                    .mentorName("David Kross")
                    .status(CareerPlanStatus.ACTIVE)
                    .description("Leading automated quality assurance frameworks and release pipelines.")
                    .startDate(LocalDateTime.now().minusMonths(10))
                    .targetCompletionDate(LocalDateTime.now().plusMonths(5))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());

            promotionCriteriaRepository.saveAll(List.of(
                    PromotionCriteria.builder()
                            .careerPlan(sarahPlan)
                            .name("Test Automation Framework")
                            .description("Architect enterprise test automation framework")
                            .isMet(true)
                            .metDate(LocalDateTime.now().minusMonths(3))
                            .type(PromotionCriteriaType.SKILL)
                            .sequenceOrder(1)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build(),
                    PromotionCriteria.builder()
                            .careerPlan(sarahPlan)
                            .name("Security Testing Review")
                            .description("Pass application penetration testing and security review")
                            .isMet(false)
                            .type(PromotionCriteriaType.ASSESSMENT)
                            .sequenceOrder(2)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build()
            ));
        }

        // 2. Seed Job Opportunities (12 matches available)
        jobOpportunityRepository.saveAll(List.of(
                JobOpportunity.builder()
                        .jobTitle("Technical Lead - Banking Core")
                        .department("Retail Banking")
                        .location("New York, NY (Hybrid)")
                        .salary(135000.0)
                        .requiredExperienceYears(5)
                        .status(JobStatus.OPEN)
                        .requiredSkills("[\"Java 17\", \"Spring Boot 4\", \"Microservices\", \"System Design\"]")
                        .description("Lead a team of 6 backend developers to scale our core retail banking ledger service. Drive design decisions, code quality, and cloud deployments.")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build(),
                JobOpportunity.builder()
                        .jobTitle("Senior Software Engineer (Angular / Spring)")
                        .department("Corporate Portals")
                        .location("Austin, TX (Remote)")
                        .salary(120000.0)
                        .requiredExperienceYears(3)
                        .status(JobStatus.OPEN)
                        .requiredSkills("[\"Java 17\", \"Spring Boot 4\", \"Angular 20\", \"System Design\"]")
                        .description("Develop and maintain interactive dashboards for corporate banking clients, integrating multi-microservice data and ensuring high security and performance.")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build(),
                JobOpportunity.builder()
                        .jobTitle("Cloud Solutions Architect")
                        .department("Cloud Platform Services")
                        .location("San Francisco, CA (Hybrid)")
                        .salary(165000.0)
                        .requiredExperienceYears(7)
                        .status(JobStatus.OPEN)
                        .requiredSkills("[\"Cloud Architecture\", \"AWS Solutions Architect Professional\", \"Kubernetes\"]")
                        .description("Help transition monolithic financial products to AWS and Azure cloud environments. Develop infra-as-code scripts and coordinate service registry networking.")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build(),
                JobOpportunity.builder()
                        .jobTitle("Quality Engineering Manager")
                        .department("Platform Assurance")
                        .location("New York, NY (Hybrid)")
                        .salary(140000.0)
                        .requiredExperienceYears(6)
                        .status(JobStatus.OPEN)
                        .requiredSkills("[\"Automation Testing\", \"CI/CD Pipelines\", \"Team Management\", \"Agile Methodologies\"]")
                        .description("Define the quality strategy for our cloud-native enterprise products. Manage a team of SDETs, design automated test suites, and coordinate release quality audits.")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build(),
                JobOpportunity.builder()
                        .jobTitle("Principal Architect - Distributed Ledger")
                        .department("Research & Innovation")
                        .location("Boston, MA (Onsite)")
                        .salary(195000.0)
                        .requiredExperienceYears(10)
                        .status(JobStatus.OPEN)
                        .requiredSkills("[\"Enterprise Architecture\", \"Distributed Systems\", \"Java 17\", \"System Design\"]")
                        .description("Lead research and prototyping of next-generation distributed transaction systems for high-frequency trading. Define framework and API guidelines.")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        ));

        log.info("Career service initial benchmark data seeded successfully.");
    }
}