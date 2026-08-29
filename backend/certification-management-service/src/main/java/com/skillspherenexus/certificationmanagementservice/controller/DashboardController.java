package com.skillspherenexus.certificationmanagementservice.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.skillspherenexus.certificationmanagementservice.dto.DashboardResponse;
import com.skillspherenexus.certificationmanagementservice.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("hasAnyRole('ADMIN','HR')") @RestController @RequestMapping("/api/dashboard") @RequiredArgsConstructor
public class DashboardController {
    private final DashboardService service;
    @GetMapping public DashboardResponse dashboard(){ return service.dashboard(); }
}
