package com.skillspherenexus.certificationmanagementservice.mapper;

import com.skillspherenexus.certificationmanagementservice.dto.CertificationResponse;
import com.skillspherenexus.certificationmanagementservice.entity.CertificationRecord;
import com.skillspherenexus.certificationmanagementservice.util.ExpiryPolicy;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class CertificationMapper {
    public CertificationResponse toResponse(CertificationRecord c) {
        return new CertificationResponse(c.getCertificationId(), c.getEmployeeId(), c.getEmployeeName(), c.getCertificationName(),
                c.getIssuingOrganization(), c.getCredentialNumber(), c.getIssueDate(), c.getExpiryDate(),
                ExpiryPolicy.daysRemaining(c.getExpiryDate(), LocalDate.now()), c.getStatus(), c.getRenewalStatus(),
                c.getVerificationStatus(), c.getComplianceStatus(), Boolean.TRUE.equals(c.getActive()), c.getWarningWindowDays(),
                c.getRenewalDueDate(), c.getLegacyCertificateId(), c.getSourceSystem(), c.getLastEvaluatedAt(), c.getCreatedAt(), c.getUpdatedAt());
    }
}
