package com.skillspherenexus.certificationmanagementservice.dto;
import java.time.LocalDateTime;
public record ReportSummaryResponse(long total, long active, long valid, long expiringSoon, long expired,
        long verified, long compliant, long renewalDue, long completedRenewals, long rejectedRenewals,
        double renewalRate, LocalDateTime generatedAt) {}
