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
 * Published to the "certificate-issued" Kafka topic whenever a new
 * certification record is registered in M3.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateIssuedEvent implements Serializable {

    private UUID certificationId;
    private Integer employeeId;
    private String employeeName;
    private String certificationName;
    private LocalDate expiryDate;
    @Builder.Default
    private String sourceService = "certification-management-service";
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();
}
