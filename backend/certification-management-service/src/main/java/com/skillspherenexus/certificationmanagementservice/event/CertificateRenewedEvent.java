package com.skillspherenexus.certificationmanagementservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Published to the "certificate-renewed" Kafka topic whenever a renewal
 * request is approved and the certification's expiry date is extended.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateRenewedEvent implements Serializable {

    private UUID certificationId;
    private Integer employeeId;
    private String employeeName;
    private String certificationName;
    private LocalDate oldExpiryDate;
    private LocalDate newExpiryDate;
    @Builder.Default
    private String sourceService = "certification-management-service";
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();
}
