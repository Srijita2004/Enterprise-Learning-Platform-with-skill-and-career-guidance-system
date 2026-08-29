package com.skillspherenexus.certificationmanagementservice.dto;

import java.util.List;

public record DashboardResponse(
        long totalCertifications, long activeCertifications, long validCertifications,
        long expiringWithin30Days, long expiredCertifications, long renewalDue,
        long pendingRenewals, long verifiedCertifications, long compliantCertifications,
        double renewalRate, List<ExpiryBucketResponse> expiryDistribution,
        List<CertificationResponse> upcomingExpirations
) {}
