package com.skillspherenexus.certificationmanagementservice.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.skillspherenexus.certificationmanagementservice.dto.*;
import com.skillspherenexus.certificationmanagementservice.enums.*;
import com.skillspherenexus.certificationmanagementservice.security.*;
import com.skillspherenexus.certificationmanagementservice.service.CertificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@PreAuthorize("hasAnyRole('ADMIN','HR','LEARNER','EMPLOYEE')") @RestController @RequestMapping("/api/certifications") @RequiredArgsConstructor
public class CertificationController {
    private final CertificationService service;

    @PostMapping
    public ResponseEntity<CertificationResponse> register(@Valid @RequestBody CertificationCreateRequest request,
            @RequestHeader(value="X-User-Id",required=false) String userId,@RequestHeader(value="X-User-Role",required=false) String role){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.register(request, RoleGuard.actor(userId,role)));
    }
    @GetMapping("/{id}") public CertificationResponse get(@PathVariable UUID id){ return service.get(id); }
    @GetMapping public PagedResponse<CertificationResponse> search(
            @RequestParam(required=false) String query,@RequestParam(required=false) CertificationStatus status,
            @RequestParam(required=false) VerificationStatus verification,@RequestParam(required=false) ComplianceStatus compliance,
            @RequestParam(required=false) String expiryBucket,@RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="10") int size,@RequestParam(defaultValue="expiryDate,asc") String sort){
        return service.search(query,status,verification,compliance,expiryBucket,page,size,sort);
    }
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PutMapping("/{id}") public CertificationResponse update(@PathVariable UUID id,@Valid @RequestBody CertificationUpdateRequest request,
            @RequestHeader(value="X-User-Id",required=false) String userId,@RequestHeader(value="X-User-Role",required=false) String role){
        return service.update(id,request,RoleGuard.manager(userId,role));
    }
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PatchMapping("/{id}/verification") public CertificationResponse verification(@PathVariable UUID id,@Valid @RequestBody VerificationUpdateRequest request,
            @RequestHeader(value="X-User-Id",required=false) String userId,@RequestHeader(value="X-User-Role",required=false) String role){
        return service.updateVerification(id,request.status(),RoleGuard.manager(userId,role));
    }
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PatchMapping("/{id}/revoke") public CertificationResponse revoke(@PathVariable UUID id,@Valid @RequestBody RevokeRequest request,
            @RequestHeader(value="X-User-Id",required=false) String userId,@RequestHeader(value="X-User-Role",required=false) String role){
        return service.revoke(id,request.reason(),RoleGuard.manager(userId,role));
    }
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PostMapping("/sync/m1") public LegacySyncResponse syncM1(@RequestHeader(value="X-User-Id",required=false) String userId,@RequestHeader(value="X-User-Role",required=false) String role){
        return service.syncFromM1(RoleGuard.manager(userId,role));
    }
}
