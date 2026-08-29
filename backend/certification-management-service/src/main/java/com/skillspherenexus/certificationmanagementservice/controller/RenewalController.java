package com.skillspherenexus.certificationmanagementservice.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.skillspherenexus.certificationmanagementservice.dto.*;
import com.skillspherenexus.certificationmanagementservice.security.*;
import com.skillspherenexus.certificationmanagementservice.service.RenewalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@PreAuthorize("hasAnyRole('ADMIN','HR')") @RestController @RequestMapping("/api/renewals") @RequiredArgsConstructor
public class RenewalController {
    private final RenewalService service;
    @GetMapping public List<RenewalRequestResponse> list(){ return service.list(); }
    @PostMapping public ResponseEntity<RenewalRequestResponse> request(@Valid @RequestBody RenewalCreateRequest request,
            @RequestHeader(value="X-User-Id",required=false) String userId,@RequestHeader(value="X-User-Role",required=false) String role){ return ResponseEntity.status(HttpStatus.CREATED).body(service.request(request,RoleGuard.manager(userId,role))); }
    @PostMapping("/{id}/approve") public RenewalRequestResponse approve(@PathVariable UUID id,@RequestBody(required=false) RenewalDecisionRequest request,
            @RequestHeader(value="X-User-Id",required=false) String userId,@RequestHeader(value="X-User-Role",required=false) String role){ return service.approve(id,request,RoleGuard.manager(userId,role)); }
    @PostMapping("/{id}/reject") public RenewalRequestResponse reject(@PathVariable UUID id,@RequestBody(required=false) RenewalDecisionRequest request,
            @RequestHeader(value="X-User-Id",required=false) String userId,@RequestHeader(value="X-User-Role",required=false) String role){ return service.reject(id,request,RoleGuard.manager(userId,role)); }
    @GetMapping("/rate") public Map<String,Double> rate(){ return Map.of("renewalRate",service.renewalRate()); }
}
