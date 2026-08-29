package com.skillspherenexus.certificationmanagementservice.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.skillspherenexus.certificationmanagementservice.dto.ReportSummaryResponse;
import com.skillspherenexus.certificationmanagementservice.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("hasAnyRole('ADMIN','HR')") @RestController @RequestMapping("/api/reports") @RequiredArgsConstructor
public class ReportController {
    private final ReportService service;
    @GetMapping("/summary") public ReportSummaryResponse summary(){ return service.summary(); }
    @GetMapping(value="/certifications.csv",produces="text/csv") public ResponseEntity<String> certificationsCsv(){ return csv("certification-report.csv",service.certificationsCsv()); }
    @GetMapping(value="/expiring.csv",produces="text/csv") public ResponseEntity<String> expiringCsv(@RequestParam(defaultValue="30") int days){ return csv("expiring-certifications-"+days+"-days.csv",service.expiringCsv(days)); }
    private ResponseEntity<String> csv(String name,String body){ return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\""+name+"\"").contentType(MediaType.parseMediaType("text/csv")).body(body); }
}
