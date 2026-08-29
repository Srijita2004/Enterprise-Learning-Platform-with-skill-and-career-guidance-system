package com.skillspherenexus.certificationmanagementservice.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.skillspherenexus.certificationmanagementservice.dto.*;
import com.skillspherenexus.certificationmanagementservice.security.*;
import com.skillspherenexus.certificationmanagementservice.service.ComplianceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@PreAuthorize("hasAnyRole('ADMIN','HR')") @RestController @RequestMapping("/api/compliance") @RequiredArgsConstructor
public class ComplianceController {
    private final ComplianceService service;
    @PostMapping("/{certificationId}/verify") public ComplianceVerificationResponse verify(@PathVariable UUID certificationId,@Valid @RequestBody ComplianceVerifyRequest request,
            @RequestHeader(value="X-User-Id",required=false) String userId,@RequestHeader(value="X-User-Role",required=false) String role){ return service.verify(certificationId,request,RoleGuard.manager(userId,role)); }
    @GetMapping("/{certificationId}/history") public List<ComplianceVerificationResponse> history(@PathVariable UUID certificationId){ return service.history(certificationId); }
    @GetMapping public List<ComplianceVerificationResponse> recent(){ return service.recent(); }
}
