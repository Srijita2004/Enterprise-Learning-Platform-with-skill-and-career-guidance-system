package com.skillspherenexus.skillmanagementservice.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class CertificateResponseDTO {

    private Integer certid;

    private Integer empid;

    private String name;

    private String issuingOrganization;

    private LocalDate issueDate;

    private LocalDate expiry;

    private String status;
}
