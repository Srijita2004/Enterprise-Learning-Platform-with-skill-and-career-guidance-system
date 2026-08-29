package com.skillspherenexus.careerservice.controller;

import com.skillspherenexus.careerservice.dto.*;
import com.skillspherenexus.careerservice.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/career")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class CareerController {

    private final CareerPlanService careerPlanService;
    private final JobPortalService jobPortalService;
    private final SkillGapAnalysisService skillGapService;
    private final TrainingAnalyticsService trainingAnalyticsService;
    private final DashboardService dashboardService;
    private final AuditLogService auditLogService;
    private final CareerRoadmapService careerRoadmapService;

    // ===== CAREER PLAN ENDPOINTS =====

    @PostMapping("/plans")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> createCareerPlan(@Valid @RequestBody CareerPlanDTO careerPlanDTO,
                                              HttpServletRequest request) {
        try {
            CareerPlanDTO created = careerPlanService.createCareerPlan(careerPlanDTO);
            auditLogService.logCreate("CAREER_PLAN", 1L, getUserId(request),
                    "Career plan created", request.getRemoteAddr());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(buildErrorResponse("Failed to create career plan: " + e.getMessage()));
        }
    }

    @GetMapping("/plans/{planId}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','LEARNER','EMPLOYEE')")
    public ResponseEntity<?> getCareerPlan(@PathVariable Long planId) {
        try {
            CareerPlanDTO plan = careerPlanService.getCareerPlan(planId);
            return ResponseEntity.ok(plan);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(buildErrorResponse("Career plan not found"));
        }
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','LEARNER','EMPLOYEE')")
    public ResponseEntity<?> getCareerPlanByEmployeeId(@PathVariable Long employeeId) {
        try {
            CareerPlanDTO plan = careerPlanService.getCareerPlanByEmployeeId(employeeId);
            return ResponseEntity.ok(plan);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(buildErrorResponse("No career plan found for this employee"));
        }
    }

    @GetMapping("/plans")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> getAllActiveCareerPlans() {
        try {
            List<CareerPlanDTO> plans = careerPlanService.getAllActiveCareerPlans();
            return ResponseEntity.ok(plans);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildErrorResponse("Error retrieving career plans: " + e.getMessage()));
        }
    }

    @PutMapping("/plans/{planId}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> updateCareerPlan(@PathVariable Long planId,
                                              @Valid @RequestBody CareerPlanDTO careerPlanDTO,
                                              HttpServletRequest request) {
        try {
            CareerPlanDTO updated = careerPlanService.updateCareerPlan(planId, careerPlanDTO);
            auditLogService.logUpdate("CAREER_PLAN", planId, "Career plan updated", getUserId(request),
                    "Updated by HR", request.getRemoteAddr());
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(buildErrorResponse("Career plan not found"));
        }
    }

    // ===== PROMOTION CRITERIA ENDPOINTS =====

    @PostMapping("/plans/{planId}/criteria/{criteriaId}/toggle")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> togglePromotionCriteria(@PathVariable Long planId,
                                                     @PathVariable Long criteriaId,
                                                     HttpServletRequest request) {
        try {
            CareerPlanDTO updated = careerPlanService.togglePromotionCriteria(planId, criteriaId);
            auditLogService.logPromotionCriteriaToggle(planId, criteriaId, true,
                    getUserId(request), request.getRemoteAddr());
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(buildErrorResponse("Career plan or criteria not found"));
        }
    }

    // ===== MENTOR ENDPOINTS =====

    @PutMapping("/plans/{planId}/mentor")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> updateMentor(@PathVariable Long planId,
                                          @RequestBody Map<String, String> body,
                                          HttpServletRequest request) {
        try {
            String mentorId = body.getOrDefault("mentorId", "");
            String mentorName = body.get("mentorName");

            if (mentorName == null || mentorName.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(buildErrorResponse("Mentor name is required"));
            }

            CareerPlanDTO updated = careerPlanService.updateMentor(planId, mentorId, mentorName);
            auditLogService.logMentorAssignment(planId, mentorId, getUserId(request), request.getRemoteAddr());
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(buildErrorResponse("Career plan not found"));
        }
    }

    // ===== SKILL GAP ENDPOINTS =====

    @GetMapping("/plans/{planId}/skill-gaps")
    @PreAuthorize("hasAnyRole('ADMIN','HR','LEARNER','EMPLOYEE')")
    public ResponseEntity<?> getSkillGaps(@PathVariable Long planId) {
        try {
            List<SkillGapDTO> gaps = skillGapService.analyzeSkillGapsForCareerPlan(planId);
            return ResponseEntity.ok(gaps);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildErrorResponse("Error retrieving skill gaps: " + e.getMessage()));
        }
    }

    @PostMapping("/plans/{planId}/skill-gaps")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> createSkillGap(@PathVariable Long planId,
                                            @Valid @RequestBody SkillGapDTO skillGapDTO,
                                            HttpServletRequest request) {
        try {
            SkillGapDTO created = skillGapService.createSkillGap(planId, skillGapDTO);
            auditLogService.logCreate("SKILL_GAP", 1L, getUserId(request),
                    "Skill gap created for plan: " + planId, request.getRemoteAddr());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(buildErrorResponse("Failed to create skill gap: " + e.getMessage()));
        }
    }

    // ===== JOB OPPORTUNITY ENDPOINTS =====

    @GetMapping("/jobs")
    @PreAuthorize("hasAnyRole('ADMIN','HR','LEARNER','EMPLOYEE')")
    public ResponseEntity<?> getAllOpenJobs() {
        try {
            List<JobOpportunityDTO> jobs = jobPortalService.getAllOpenJobs();
            return ResponseEntity.ok(jobs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildErrorResponse("Error retrieving jobs: " + e.getMessage()));
        }
    }

    @PostMapping("/jobs")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> createJobOpportunity(@Valid @RequestBody JobOpportunityDTO jobDTO,
                                                  HttpServletRequest request) {
        try {
            JobOpportunityDTO created = jobPortalService.createJobOpportunity(jobDTO);
            auditLogService.logCreate("JOB_OPPORTUNITY", 1L, getUserId(request),
                    "Job posted", request.getRemoteAddr());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(buildErrorResponse("Failed to create job: " + e.getMessage()));
        }
    }

    @GetMapping("/jobs/matching/{planId}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','LEARNER','EMPLOYEE')")
    public ResponseEntity<?> getMatchingJobs(@PathVariable Long planId) {
        try {
            List<JobOpportunityDTO> matchingJobs = jobPortalService.findMatchingJobsForCareerPlan(planId);
            return ResponseEntity.ok(matchingJobs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildErrorResponse("Error finding matching jobs: " + e.getMessage()));
        }
    }

    @GetMapping("/jobs/{jobId}/match/{planId}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','LEARNER','EMPLOYEE')")
    public ResponseEntity<?> calculateJobMatch(@PathVariable Long jobId, @PathVariable Long planId) {
        try {
            Integer matchScore = jobPortalService.calculateJobMatchScore(planId, jobId);
            Map<String, Object> response = new HashMap<>();
            response.put("jobId", jobId);
            response.put("planId", planId);
            response.put("matchScore", matchScore);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildErrorResponse("Error calculating match: " + e.getMessage()));
        }
    }

    // ===== CAREER ROADMAP ENDPOINTS =====

    @GetMapping("/roadmaps")
    @PreAuthorize("hasAnyRole('ADMIN','HR','LEARNER','EMPLOYEE')")
    public ResponseEntity<?> getAllActiveRoadmaps() {
        try {
            List<CareerRoadmapDTO> roadmaps = careerRoadmapService.getAllActiveRoadmaps();
            return ResponseEntity.ok(roadmaps);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildErrorResponse("Error retrieving roadmaps: " + e.getMessage()));
        }
    }

    @GetMapping("/roadmaps/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','LEARNER','EMPLOYEE')")
    public ResponseEntity<?> getRoadmap(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(careerRoadmapService.getRoadmap(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(buildErrorResponse("Roadmap not found"));
        }
    }

    @GetMapping("/roadmaps/match")
    @PreAuthorize("hasAnyRole('ADMIN','HR','LEARNER','EMPLOYEE')")
    public ResponseEntity<?> findRoadmapByRoles(@RequestParam String sourceRole,
                                                @RequestParam String targetRole) {
        return careerRoadmapService.findRoadmapByRoles(sourceRole, targetRole)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(buildErrorResponse("No roadmap found for these roles")));
    }

    @PostMapping("/roadmaps")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> createRoadmap(@Valid @RequestBody CareerRoadmapDTO dto,
                                           HttpServletRequest request) {
        try {
            CareerRoadmapDTO created = careerRoadmapService.createRoadmap(dto);
            auditLogService.logCreate("CAREER_ROADMAP", created.getId(), getUserId(request),
                    "Roadmap created: " + dto.getSourceRole() + " -> " + dto.getTargetRole(),
                    request.getRemoteAddr());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(buildErrorResponse("Failed to create roadmap: " + e.getMessage()));
        }
    }

    @PutMapping("/roadmaps/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> updateRoadmap(@PathVariable Long id,
                                           @Valid @RequestBody CareerRoadmapDTO dto) {
        try {
            return ResponseEntity.ok(careerRoadmapService.updateRoadmap(id, dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(buildErrorResponse("Roadmap not found"));
        }
    }

    // ===== TRAINING ANALYTICS ENDPOINTS =====

    @GetMapping("/training/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN','HR','LEARNER','EMPLOYEE')")
    public ResponseEntity<?> getTrainingRecordsByEmployee(@PathVariable Long employeeId) {
        try {
            List<TrainingRecordDTO> records = trainingAnalyticsService.getTrainingRecordsByEmployee(employeeId);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildErrorResponse("Error retrieving training records: " + e.getMessage()));
        }
    }

    @GetMapping("/training/employee/{employeeId}/average-score")
    @PreAuthorize("hasAnyRole('ADMIN','HR','LEARNER','EMPLOYEE')")
    public ResponseEntity<?> getAverageEmployeeScore(@PathVariable Long employeeId) {
        Double avg = trainingAnalyticsService.getAverageEmployeeScore(employeeId);
        Map<String, Object> response = new HashMap<>();
        response.put("employeeId", employeeId);
        response.put("averageScore", avg);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/training/course/{courseId}/completion-rate")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> getCourseCompletionRate(@PathVariable String courseId) {
        Integer rate = trainingAnalyticsService.getCompletionRatePercentage(courseId);
        Map<String, Object> response = new HashMap<>();
        response.put("courseId", courseId);
        response.put("completionRatePercentage", rate);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/training")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ResponseEntity<?> createTrainingRecord(@Valid @RequestBody TrainingRecordDTO dto,
                                                  HttpServletRequest request) {
        try {
            TrainingRecordDTO created = trainingAnalyticsService.createTrainingRecord(dto);
            auditLogService.logCreate("TRAINING_RECORD", 1L, getUserId(request),
                    "Training record created", request.getRemoteAddr());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(buildErrorResponse("Failed to create training record: " + e.getMessage()));
        }
    }

    // ===== DASHBOARD ENDPOINTS =====

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','HR','LEARNER','EMPLOYEE')")
    public ResponseEntity<?> getExecutiveDashboard() {
        try {
            CareerDashboardDTO dashboard = dashboardService.getExecutiveDashboard();
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildErrorResponse("Error retrieving dashboard: " + e.getMessage()));
        }
    }

    // ===== HELPER METHODS =====

    private Long getUserId(HttpServletRequest request) {
        String userIdHeader = request.getHeader("X-User-Id");
        try {
            return userIdHeader != null && !userIdHeader.isBlank() ? Long.parseLong(userIdHeader) : 0L;
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private Map<String, Object> buildErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ERROR");
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    // Self-contained static classes for the API response objects (DEPRECATED - kept for backward compatibility)
    @Deprecated
    public static class PromotionCriteria {
        public int criteriaId;
        public String name;
        public String description;
        public boolean isMet;
        public String type;

        public PromotionCriteria() {}
        public PromotionCriteria(int criteriaId, String name, String description, boolean isMet, String type) {
            this.criteriaId = criteriaId;
            this.name = name;
            this.description = description;
            this.isMet = isMet;
            this.type = type;
        }
    }
}