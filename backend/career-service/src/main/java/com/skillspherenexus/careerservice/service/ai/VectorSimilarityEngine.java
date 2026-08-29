package com.skillspherenexus.careerservice.service.ai;

import com.skillspherenexus.careerservice.dto.AiCareerEvaluationRequestDTO;
import com.skillspherenexus.careerservice.dto.AiSkillGapDTO;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class VectorSimilarityEngine {

    // Benchmark Skill Requirements for Target Roles (SkillName -> Required Level 1 to 5)
    private static final Map<String, Map<String, Integer>> ROLE_BENCHMARKS = new LinkedHashMap<>();

    static {
        // Senior Full-Stack / Java Architect
        Map<String, Integer> fullstack = new LinkedHashMap<>();
        fullstack.put("Java 17", 5);
        fullstack.put("Spring Boot 4", 5);
        fullstack.put("Microservices Architecture", 4);
        fullstack.put("System Design & Scalability", 4);
        fullstack.put("Angular 20", 4);
        fullstack.put("PostgreSQL", 4);
        ROLE_BENCHMARKS.put("Senior Full-Stack Engineer", fullstack);
        ROLE_BENCHMARKS.put("Lead Java Developer", fullstack);
        ROLE_BENCHMARKS.put("Principal Engineer", fullstack);

        // Cloud Solutions Architect
        Map<String, Integer> cloud = new LinkedHashMap<>();
        cloud.put("AWS Cloud Infrastructure", 5);
        cloud.put("Kubernetes & Docker", 5);
        cloud.put("Microservices Architecture", 4);
        cloud.put("System Design & Scalability", 5);
        cloud.put("PostgreSQL", 4);
        ROLE_BENCHMARKS.put("Cloud Solutions Architect", cloud);
        ROLE_BENCHMARKS.put("Senior Cloud Engineer", cloud);
        ROLE_BENCHMARKS.put("DevOps Lead", cloud);

        // QA Lead / Test Architect
        Map<String, Integer> qa = new LinkedHashMap<>();
        qa.put("Automated Testing & QA", 5);
        qa.put("Java 17", 4);
        qa.put("Agile & Team Leadership", 4);
        qa.put("Microservices Architecture", 3);
        ROLE_BENCHMARKS.put("QA Lead", qa);
        ROLE_BENCHMARKS.put("Test Automation Architect", qa);

        // Senior Professional Specialist / General Growth
        Map<String, Integer> generalSenior = new LinkedHashMap<>();
        generalSenior.put("Java 17", 4);
        generalSenior.put("Spring Boot 4", 4);
        generalSenior.put("Microservices Architecture", 3);
        generalSenior.put("PostgreSQL", 3);
        generalSenior.put("Agile & Team Leadership", 3);
        ROLE_BENCHMARKS.put("Senior Professional Specialist", generalSenior);
        ROLE_BENCHMARKS.put("Senior Developer", generalSenior);
    }

    public Map<String, Integer> getTargetRoleBenchmark(String targetRole) {
        if (targetRole == null || targetRole.isBlank()) {
            return ROLE_BENCHMARKS.get("Senior Professional Specialist");
        }
        for (Map.Entry<String, Map<String, Integer>> entry : ROLE_BENCHMARKS.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(targetRole.trim()) || 
                targetRole.toLowerCase().contains(entry.getKey().toLowerCase())) {
                return entry.getValue();
            }
        }
        return ROLE_BENCHMARKS.get("Senior Professional Specialist");
    }

    /**
     * Mathematical Cosine Similarity Vector Space Engine
     * cos(theta) = (u . v) / (||u|| * ||v||)
     */
    public double calculateCosineSimilarity(Map<String, Double> employeeVector, Map<String, Double> targetVector) {
        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(employeeVector.keySet());
        allKeys.addAll(targetVector.keySet());

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (String key : allKeys) {
            double a = employeeVector.getOrDefault(key, 0.0);
            double b = targetVector.getOrDefault(key, 0.0);
            dotProduct += a * b;
            normA += a * a;
            normB += b * b;
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        return Math.min(1.0, Math.max(0.0, dotProduct / (Math.sqrt(normA) * Math.sqrt(normB))));
    }

    /**
     * Converts employee skills, assessment scores, and certs into a normalized feature vector
     */
    public Map<String, Double> buildEmployeeVector(AiCareerEvaluationRequestDTO request) {
        Map<String, Double> vector = new LinkedHashMap<>();

        // Map skills with proficiency (level 1-5 normalized to 0.2 - 1.0)
        if (request.getSkills() != null) {
            for (AiCareerEvaluationRequestDTO.SkillInput skill : request.getSkills()) {
                double levelWeight = (skill.getProficiencyLevel() != null ? skill.getProficiencyLevel() : 1) * 1.0;
                
                // Assessment score booster if present
                if (request.getAssessments() != null) {
                    for (AiCareerEvaluationRequestDTO.AssessmentInput a : request.getAssessments()) {
                        if (a.getSkillId() != null && a.getSkillId().equals(skill.getSkillId()) && a.getScore() != null) {
                            levelWeight += (a.getScore() / 100.0) * 0.5; // up to 0.5 bonus weight
                        }
                    }
                }
                vector.put(normalizeSkillName(skill.getSkillName()), levelWeight);
            }
        }

        return vector;
    }

    public Map<String, Double> buildTargetBenchmarkVector(Map<String, Integer> benchmark) {
        Map<String, Double> vector = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : benchmark.entrySet()) {
            vector.put(normalizeSkillName(entry.getKey()), entry.getValue() * 1.0);
        }
        return vector;
    }

    public List<AiSkillGapDTO> calculateSkillGaps(Map<String, Double> employeeVector, Map<String, Integer> benchmark) {
        List<AiSkillGapDTO> gaps = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : benchmark.entrySet()) {
            String normalized = normalizeSkillName(entry.getKey());
            int requiredLevel = entry.getValue();
            double currentVal = employeeVector.getOrDefault(normalized, 0.0);
            int currentLevel = (int) Math.round(Math.min(5.0, currentVal));

            if (currentLevel < requiredLevel) {
                int gap = requiredLevel - currentLevel;
                String priority = gap >= 2 ? "HIGH" : "MEDIUM";
                String recAction = "Advance from Level " + currentLevel + " to Level " + requiredLevel + " via targeted modules.";
                String course = resolveCourseForSkill(entry.getKey());

                gaps.add(AiSkillGapDTO.builder()
                        .skillName(entry.getKey())
                        .currentLevel(currentLevel)
                        .requiredLevel(requiredLevel)
                        .gapLevel(gap)
                        .priority(priority)
                        .recommendedAction(recAction)
                        .targetCourse(course)
                        .build());
            }
        }

        return gaps;
    }

    private String resolveCourseForSkill(String skillName) {
        if (skillName == null) return "Enterprise System Design Patterns & Distributed Scalability";
        String lower = skillName.toLowerCase();
        if (lower.contains("angular")) {
            return "Modern Web Engineering with Angular 20";
        } else if (lower.contains("spring") || lower.contains("microservices")) {
            return "Spring Boot 4 & Cloud Microservices Architecture";
        } else if (lower.contains("java")) {
            return "Advanced Java 17 & Reactive Concurrency Masterclass";
        } else if (lower.contains("system design") || lower.contains("scalability") || lower.contains("architecture")) {
            return "Enterprise System Design Patterns & Distributed Scalability";
        } else if (lower.contains("postgres") || lower.contains("sql") || lower.contains("database")) {
            return "Advanced PostgreSQL & High-Performance Data Architecture";
        } else if (lower.contains("docker") || lower.contains("kubernetes") || lower.contains("devops") || lower.contains("ci/cd")) {
            return "Enterprise CI/CD, Docker & Container Orchestration";
        } else if (lower.contains("aws") || lower.contains("cloud")) {
            return "Cloud Solutions Architecture & AWS Infrastructure";
        } else if (lower.contains("test") || lower.contains("qa") || lower.contains("automation")) {
            return "Test Automation, Quality Engineering & CI Verification";
        } else if (lower.contains("lead") || lower.contains("agile") || lower.contains("management")) {
            return "Agile Engineering Leadership & Team Mentorship";
        }
        return "Enterprise System Design Patterns & Distributed Scalability";
    }

    /**
     * Calibrated Multi-Criteria Promotion Readiness Probability
     */
    public double calculatePromotionReadiness(double cosineSim, AiCareerEvaluationRequestDTO request, int gapCount) {
        double baseScore = cosineSim * 50.0; // 0 to 50 points from vector cosine

        // Assessment performance (0 to 25 points)
        double assessmentFactor = 15.0;
        if (request.getAssessments() != null && !request.getAssessments().isEmpty()) {
            double avg = request.getAssessments().stream()
                    .filter(a -> a.getScore() != null)
                    .mapToDouble(AiCareerEvaluationRequestDTO.AssessmentInput::getScore)
                    .average()
                    .orElse(75.0);
            assessmentFactor = (avg / 100.0) * 25.0;
        }

        // Certifications factor (0 to 15 points)
        int certCount = request.getCertifications() != null ? request.getCertifications().size() : 0;
        double certFactor = Math.min(15.0, certCount * 5.0);

        // Experience & Gap penalty (0 to 10 points)
        int exp = request.getYearsExperience() != null ? request.getYearsExperience() : 2;
        double expFactor = Math.min(10.0, exp * 2.5);
        double gapPenalty = Math.min(20.0, gapCount * 3.5);

        double total = baseScore + assessmentFactor + certFactor + expFactor - gapPenalty;
        return Math.max(15.0, Math.min(96.0, Math.round(total * 10.0) / 10.0));
    }

    private String normalizeSkillName(String skill) {
        if (skill == null) return "";
        return skill.toLowerCase().replaceAll("[^a-z0-9]", "");
    }
}
