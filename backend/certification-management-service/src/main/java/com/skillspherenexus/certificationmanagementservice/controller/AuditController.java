package com.skillspherenexus.certificationmanagementservice.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.skillspherenexus.certificationmanagementservice.dto.AuditLogResponse;
import com.skillspherenexus.certificationmanagementservice.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@PreAuthorize("hasAnyRole('ADMIN','HR')") @RestController @RequestMapping("/api/audit") @RequiredArgsConstructor
public class AuditController {
    private final AuditService service;
    @GetMapping public List<AuditLogResponse> recent(){ return service.recent(); }
    @GetMapping("/certifications/{certificationId}") public List<AuditLogResponse> byCertification(@PathVariable UUID certificationId){ return service.byCertification(certificationId); }
}
