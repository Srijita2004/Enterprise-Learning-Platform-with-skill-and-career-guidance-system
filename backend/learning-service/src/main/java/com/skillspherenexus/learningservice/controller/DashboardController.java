package com.skillspherenexus.learningservice.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.skillspherenexus.learningservice.dto.HrDashboardResponseDTO;
import com.skillspherenexus.learningservice.dto.LearnerDashboardResponseDTO;
import com.skillspherenexus.learningservice.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.UUID;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/hr")
    public ResponseEntity<HrDashboardResponseDTO> getHrDashboard(
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        String normalizedRole = userRole == null
                ? ""
                : userRole.trim().toUpperCase(Locale.ROOT);

        if (!normalizedRole.equals("ADMIN") && !normalizedRole.equals("HR")) {
            throw new IllegalArgumentException("Only Admin or HR can access the HR dashboard");
        }

        return ResponseEntity.ok(dashboardService.getHrDashboard());
    }

    @GetMapping("/learners/{learnerId}")
    public ResponseEntity<LearnerDashboardResponseDTO>
    getLearnerDashboard(
            @PathVariable UUID learnerId
    ) {
        LearnerDashboardResponseDTO response =
                dashboardService.getLearnerDashboard(
                        learnerId
                );

        return ResponseEntity.ok(response);
    }
}