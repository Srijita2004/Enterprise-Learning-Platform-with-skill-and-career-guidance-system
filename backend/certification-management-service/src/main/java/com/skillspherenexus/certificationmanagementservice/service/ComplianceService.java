package com.skillspherenexus.certificationmanagementservice.service;

import com.skillspherenexus.certificationmanagementservice.dto.*;
import com.skillspherenexus.certificationmanagementservice.entity.CertificationRecord;
import com.skillspherenexus.certificationmanagementservice.enums.*;
import com.skillspherenexus.certificationmanagementservice.security.RequestActor;
import java.util.*;

public interface ComplianceService {
    ComplianceVerificationResponse verify(UUID certificationId, ComplianceVerifyRequest request, RequestActor actor);
    List<ComplianceVerificationResponse> history(UUID certificationId);
    List<ComplianceVerificationResponse> recent();
}
