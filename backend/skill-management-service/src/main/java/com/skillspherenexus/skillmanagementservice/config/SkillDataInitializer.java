package com.skillspherenexus.skillmanagementservice.config;

import com.skillspherenexus.skillmanagementservice.entity.*;
import com.skillspherenexus.skillmanagementservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SkillDataInitializer implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final SkillRepository skillRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final AssessmentRepository assessmentRepository;
    private final CompetencyRepository competencyRepository;
    private final CompetencyFrameworkRepository competencyFrameworkRepository;
    private final EmployeeCompetencyRepository employeeCompetencyRepository;

    @Override
    public void run(String... args) {
        if (employeeRepository.count() > 0) {
            log.info("Employees already exist ({} records). Skipping skill service seed.", employeeRepository.count());
            return;
        }

        log.info("Seeding initial benchmark enterprise employees, skills, assessments, and competencies...");

        // 1. Employees (All 10 Core Enterprise Employees)
        List<Employee> employees = List.of(
                new Employee(1, "Srijita", "Senior Java Developer", 115000.0),
                new Employee(101, "Alex Vance", "Cloud Infrastructure Engineer", 110000.0),
                new Employee(102, "Marcus Brodie", "Principal Systems Architect", 145000.0),
                new Employee(103, "Sarah Jenkins", "Senior QA Lead", 98000.0),
                new Employee(106, "John Smith", "Frontend Software Engineer", 102000.0),
                new Employee(107, "Jane Doe", "Data & Machine Learning Engineer", 125000.0),
                new Employee(108, "David Miller", "DevOps & Site Reliability Engineer", 118000.0),
                new Employee(109, "Elena Rostova", "Cybersecurity & Compliance Analyst", 130000.0),
                new Employee(110, "Michael Chang", "Full-Stack Application Developer", 108000.0),
                new Employee(111, "Priya Sharma", "Database Performance Specialist", 122000.0)
        );
        employeeRepository.saveAll(employees);

        // 2. Skills
        List<Skill> skills = List.of(
                new Skill(1, "Java 17 & Concurrency", "TECHNICAL", "Core Java, Virtual Threads, Streams, Memory Optimization, JVM Tuning"),
                new Skill(2, "Spring Boot 4 & Cloud", "TECHNICAL", "Microservices architectures, Spring Cloud Gateway, Eureka, Hibernate, Spring Security"),
                new Skill(3, "Angular 21 & Signals", "TECHNICAL", "TypeScript SPA Framework, Standalone Architecture, Signals, Sub-second LCP"),
                new Skill(4, "PostgreSQL & Query Tuning", "TECHNICAL", "Relational Database, complex indexing, partitioning, query optimization"),
                new Skill(5, "Kubernetes & Multi-Cloud", "TECHNICAL", "Container orchestration, Helm charts, service mesh, zero-downtime rollouts"),
                new Skill(6, "Automated QA & Testing", "TECHNICAL", "JUnit, Mockito, Cypress, Playwright, performance benchmarking"),
                new Skill(7, "Zero-Trust Cybersecurity", "DOMAIN", "JWT authentication, RBAC authorization, TLS encryption, SOC2 compliance"),
                new Skill(8, "AI & Vector MLOps", "TECHNICAL", "Vector embeddings, cosine similarity search, Gemini LLM prompt orchestration"),
                new Skill(9, "DevOps & Terraform IaC", "TECHNICAL", "CI/CD pipeline automation, Docker containerization, Infrastructure as Code"),
                new Skill(10, "Distributed System Design", "TECHNICAL", "Event-driven architecture, Apache Kafka messaging, CAP theorem, resilience")
        );
        skillRepository.saveAll(skills);

        // 3. Employee Skills
        List<EmployeeSkill> empSkills = List.of(
                new EmployeeSkill(101, 1, 1, 5, 6),
                new EmployeeSkill(102, 1, 2, 5, 5),
                new EmployeeSkill(103, 1, 4, 4, 4),
                new EmployeeSkill(104, 1, 10, 4, 4),
                new EmployeeSkill(201, 101, 5, 5, 7),
                new EmployeeSkill(202, 101, 9, 5, 6),
                new EmployeeSkill(301, 102, 10, 5, 12),
                new EmployeeSkill(302, 102, 5, 5, 10),
                new EmployeeSkill(401, 103, 6, 5, 8),
                new EmployeeSkill(501, 106, 3, 5, 6),
                new EmployeeSkill(601, 107, 8, 5, 7),
                new EmployeeSkill(701, 108, 9, 5, 8),
                new EmployeeSkill(801, 109, 7, 5, 9),
                new EmployeeSkill(901, 110, 1, 4, 5),
                new EmployeeSkill(1001, 111, 4, 5, 9)
        );
        employeeSkillRepository.saveAll(empSkills);

        // 4. Assessments
        List<Assessment> assessments = List.of(
                new Assessment(101L, 1L, 1L, 96, true),
                new Assessment(102L, 1L, 2L, 94, true),
                new Assessment(103L, 1L, 10L, 90, true),
                new Assessment(201L, 101L, 5L, 98, true),
                new Assessment(202L, 101L, 9L, 92, true),
                new Assessment(301L, 102L, 10L, 99, true),
                new Assessment(401L, 103L, 6L, 97, true),
                new Assessment(501L, 106L, 3L, 95, true),
                new Assessment(601L, 107L, 8L, 96, true),
                new Assessment(701L, 108L, 9L, 94, true),
                new Assessment(801L, 109L, 7L, 98, true),
                new Assessment(901L, 110L, 1L, 89, true),
                new Assessment(1001L, 111L, 4L, 96, true)
        );
        assessmentRepository.saveAll(assessments);

        // 5. Competencies
        Competency c1 = new Competency();
        c1.setName("Software Architecture");
        c1.setCategory(CompetencyCategory.TECHNICAL);
        c1.setDescription("System modeling, design patterns, microservices choreography, resilience");
        c1.setMaxLevel(5);
        c1 = competencyRepository.save(c1);

        Competency c2 = new Competency();
        c2.setName("Cloud Native & Kubernetes");
        c2.setCategory(CompetencyCategory.TECHNICAL);
        c2.setDescription("Multi-region Kubernetes, Terraform IaC, Docker containerization");
        c2.setMaxLevel(5);
        c2 = competencyRepository.save(c2);

        Competency c3 = new Competency();
        c3.setName("Microservices & Event Streaming");
        c3.setCategory(CompetencyCategory.TECHNICAL);
        c3.setDescription("Kafka event fabrics, reactive messaging, Spring Cloud Gateway");
        c3.setMaxLevel(5);
        c3 = competencyRepository.save(c3);

        Competency c4 = new Competency();
        c4.setName("Problem Solving & Algorithms");
        c4.setCategory(CompetencyCategory.SOFT);
        c4.setDescription("Analytical reasoning, algorithmic optimization, distributed complexity");
        c4.setMaxLevel(5);
        c4 = competencyRepository.save(c4);

        Competency c5 = new Competency();
        c5.setName("Technical Leadership & Mentorship");
        c5.setCategory(CompetencyCategory.SOFT);
        c5.setDescription("Architecture review boards, engineering mentoring, talent development");
        c5.setMaxLevel(5);
        c5 = competencyRepository.save(c5);

        log.info("Skill service data initialization completed successfully.");
    }
}
