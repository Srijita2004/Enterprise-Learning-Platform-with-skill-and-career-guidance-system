package com.skillspherenexus.certificationmanagementservice.integration;
import java.time.LocalDate;
public record LegacyCertificate(Integer certid, Integer empid, String name, String issuingOrganization, LocalDate issueDate, LocalDate expiry, String status) {}
