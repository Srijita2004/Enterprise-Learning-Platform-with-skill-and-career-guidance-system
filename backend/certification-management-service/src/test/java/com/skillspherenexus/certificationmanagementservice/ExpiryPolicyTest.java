package com.skillspherenexus.certificationmanagementservice;

import com.skillspherenexus.certificationmanagementservice.enums.CertificationStatus;
import com.skillspherenexus.certificationmanagementservice.util.ExpiryPolicy;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExpiryPolicyTest {
    private final LocalDate today = LocalDate.of(2026, 8, 8);

    @Test
    void classifiesCoreExpiryBoundaries() {
        assertEquals(CertificationStatus.EXPIRED, ExpiryPolicy.status(today.minusDays(1), today, 30, true));
        assertEquals(CertificationStatus.EXPIRING_SOON, ExpiryPolicy.status(today, today, 30, true));
        assertEquals(CertificationStatus.EXPIRING_SOON, ExpiryPolicy.status(today.plusDays(30), today, 30, true));
        assertEquals(CertificationStatus.VALID, ExpiryPolicy.status(today.plusDays(31), today, 30, true));
        assertEquals(CertificationStatus.NO_EXPIRY, ExpiryPolicy.status(null, today, 30, true));
        assertEquals(CertificationStatus.REVOKED, ExpiryPolicy.status(today.plusDays(90), today, 30, false));
    }
}
