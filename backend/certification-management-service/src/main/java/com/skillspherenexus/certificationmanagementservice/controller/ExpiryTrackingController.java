package com.skillspherenexus.certificationmanagementservice.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.skillspherenexus.certificationmanagementservice.dto.*;
import com.skillspherenexus.certificationmanagementservice.security.*;
import com.skillspherenexus.certificationmanagementservice.service.ExpiryTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@PreAuthorize("hasAnyRole('ADMIN','HR')") @RestController @RequestMapping("/api/expiry") @RequiredArgsConstructor
public class ExpiryTrackingController {
    private final ExpiryTrackingService service;
    @GetMapping("/expiring") public List<CertificationResponse> expiring(@RequestParam(defaultValue="30") int days){ return service.expiringWithin(days); }
    @GetMapping("/expired") public List<CertificationResponse> expired(){ return service.expired(); }
    @GetMapping("/distribution") public List<ExpiryBucketResponse> distribution(){ return service.distribution(); }
    @PostMapping("/evaluate") public BulkEvaluationResponse evaluate(@RequestHeader(value="X-User-Id",required=false) String userId,@RequestHeader(value="X-User-Role",required=false) String role){ return service.evaluateAll(RoleGuard.manager(userId,role)); }
}
