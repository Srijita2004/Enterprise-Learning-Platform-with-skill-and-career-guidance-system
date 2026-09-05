package com.skillspherenexus.certificationmanagementservice.config;

import com.skillspherenexus.certificationmanagementservice.entity.CertificationRecord;
import com.skillspherenexus.certificationmanagementservice.enums.CertificationStatus;
import com.skillspherenexus.certificationmanagementservice.enums.ComplianceStatus;
import com.skillspherenexus.certificationmanagementservice.enums.RenewalStatus;
import com.skillspherenexus.certificationmanagementservice.enums.VerificationStatus;
import com.skillspherenexus.certificationmanagementservice.repository.CertificationRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CertificationDataInitializer implements CommandLineRunner {

    private final CertificationRecordRepository certificationRecordRepository;

    @Override
    public void run(String... args) {
        if (certificationRecordRepository.count() > 0) {
            log.info("Certification records already exist ({} records). Skipping seed.", certificationRecordRepository.count());
            return;
        }

        log.info("Seeding initial benchmark verified professional certifications across employees...");

        List<CertificationRecord> records = List.of(
                CertificationRecord.builder()
                        .employeeId(1)
                        .employeeName("Srijita")
                        .certificationName("AWS Certified Solutions Architect - Professional")
                        .issuingOrganization("Amazon Web Services")
                        .credentialNumber("AWS-SAP-981024")
                        .issueDate(LocalDate.now().minusMonths(6))
                        .expiryDate(LocalDate.now().plusMonths(30))
                        .status(CertificationStatus.VALID)
                        .renewalStatus(RenewalStatus.NOT_REQUIRED)
                        .verificationStatus(VerificationStatus.VERIFIED)
                        .complianceStatus(ComplianceStatus.COMPLIANT)
                        .active(true)
                        .warningWindowDays(30)
                        .sourceSystem("ENTERPRISE_CORE")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build(),
                CertificationRecord.builder()
                        .employeeId(1)
                        .employeeName("Srijita")
                        .certificationName("Spring Cloud Microservices Developer")
                        .issuingOrganization("VMware Tanzu")
                        .credentialNumber("VMW-SCMD-441098")
                        .issueDate(LocalDate.now().minusMonths(12))
                        .expiryDate(LocalDate.now().plusMonths(24))
                        .status(CertificationStatus.VALID)
                        .renewalStatus(RenewalStatus.NOT_REQUIRED)
                        .verificationStatus(VerificationStatus.VERIFIED)
                        .complianceStatus(ComplianceStatus.COMPLIANT)
                        .active(true)
                        .warningWindowDays(30)
                        .sourceSystem("ENTERPRISE_CORE")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build(),
                CertificationRecord.builder()
                        .employeeId(101)
                        .employeeName("Alex Vance")
                        .certificationName("Certified Kubernetes Administrator (CKA)")
                        .issuingOrganization("Cloud Native Computing Foundation")
                        .credentialNumber("CKA-2024-88910")
                        .issueDate(LocalDate.now().minusMonths(5))
                        .expiryDate(LocalDate.now().plusMonths(31))
                        .status(CertificationStatus.VALID)
                        .renewalStatus(RenewalStatus.NOT_REQUIRED)
                        .verificationStatus(VerificationStatus.VERIFIED)
                        .complianceStatus(ComplianceStatus.COMPLIANT)
                        .active(true)
                        .warningWindowDays(30)
                        .sourceSystem("ENTERPRISE_CORE")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build(),
                CertificationRecord.builder()
                        .employeeId(102)
                        .employeeName("Marcus Brodie")
                        .certificationName("TOGAF 9.2 Certified Enterprise Architect")
                        .issuingOrganization("The Open Group")
                        .credentialNumber("TOGAF-92-100293")
                        .issueDate(LocalDate.now().minusMonths(18))
                        .expiryDate(LocalDate.now().plusMonths(42))
                        .status(CertificationStatus.VALID)
                        .renewalStatus(RenewalStatus.NOT_REQUIRED)
                        .verificationStatus(VerificationStatus.VERIFIED)
                        .complianceStatus(ComplianceStatus.COMPLIANT)
                        .active(true)
                        .warningWindowDays(30)
                        .sourceSystem("ENTERPRISE_CORE")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build(),
                CertificationRecord.builder()
                        .employeeId(103)
                        .employeeName("Sarah Jenkins")
                        .certificationName("ISTQB Advanced Level Test Automation Engineer")
                        .issuingOrganization("ISTQB")
                        .credentialNumber("ISTQB-ALTA-33019")
                        .issueDate(LocalDate.now().minusMonths(8))
                        .expiryDate(LocalDate.now().plusMonths(28))
                        .status(CertificationStatus.VALID)
                        .renewalStatus(RenewalStatus.NOT_REQUIRED)
                        .verificationStatus(VerificationStatus.VERIFIED)
                        .complianceStatus(ComplianceStatus.COMPLIANT)
                        .active(true)
                        .warningWindowDays(30)
                        .sourceSystem("ENTERPRISE_CORE")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build(),
                CertificationRecord.builder()
                        .employeeId(107)
                        .employeeName("Jane Doe")
                        .certificationName("Google Cloud Professional Machine Learning Engineer")
                        .issuingOrganization("Google Cloud")
                        .credentialNumber("GCP-PMLE-772109")
                        .issueDate(LocalDate.now().minusMonths(3))
                        .expiryDate(LocalDate.now().plusMonths(21))
                        .status(CertificationStatus.VALID)
                        .renewalStatus(RenewalStatus.NOT_REQUIRED)
                        .verificationStatus(VerificationStatus.VERIFIED)
                        .complianceStatus(ComplianceStatus.COMPLIANT)
                        .active(true)
                        .warningWindowDays(30)
                        .sourceSystem("ENTERPRISE_CORE")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build(),
                CertificationRecord.builder()
                        .employeeId(109)
                        .employeeName("Elena Rostova")
                        .certificationName("Certified Information Systems Security Professional (CISSP)")
                        .issuingOrganization("ISC2")
                        .credentialNumber("CISSP-882190")
                        .issueDate(LocalDate.now().minusMonths(14))
                        .expiryDate(LocalDate.now().plusMonths(22))
                        .status(CertificationStatus.VALID)
                        .renewalStatus(RenewalStatus.NOT_REQUIRED)
                        .verificationStatus(VerificationStatus.VERIFIED)
                        .complianceStatus(ComplianceStatus.COMPLIANT)
                        .active(true)
                        .warningWindowDays(30)
                        .sourceSystem("ENTERPRISE_CORE")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );

        certificationRecordRepository.saveAll(records);
        log.info("Certification management service data initialization completed successfully.");
    }
}
