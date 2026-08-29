package com.skillspherenexus.certificationmanagementservice.util;

import com.skillspherenexus.certificationmanagementservice.enums.CertificationStatus;
import java.time.*;
import java.time.temporal.ChronoUnit;

public final class ExpiryPolicy {
    private ExpiryPolicy() {}
    public static CertificationStatus status(LocalDate expiryDate, LocalDate today, int warningDays, boolean active) {
        if (!active) return CertificationStatus.REVOKED;
        if (expiryDate == null) return CertificationStatus.NO_EXPIRY;
        if (expiryDate.isBefore(today)) return CertificationStatus.EXPIRED;
        if (!expiryDate.isAfter(today.plusDays(warningDays))) return CertificationStatus.EXPIRING_SOON;
        return CertificationStatus.VALID;
    }
    public static Long daysRemaining(LocalDate expiryDate, LocalDate today) {
        return expiryDate == null ? null : ChronoUnit.DAYS.between(today, expiryDate);
    }
}
