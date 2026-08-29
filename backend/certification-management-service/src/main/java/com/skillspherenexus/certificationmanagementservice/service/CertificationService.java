package com.skillspherenexus.certificationmanagementservice.service;

import com.skillspherenexus.certificationmanagementservice.dto.*;
import com.skillspherenexus.certificationmanagementservice.entity.CertificationRecord;
import com.skillspherenexus.certificationmanagementservice.enums.*;
import com.skillspherenexus.certificationmanagementservice.security.RequestActor;
import java.util.*;

public interface CertificationService {
    CertificationResponse register(CertificationCreateRequest request, RequestActor actor);
    CertificationResponse update(UUID id, CertificationUpdateRequest request, RequestActor actor);
    CertificationResponse get(UUID id);
    PagedResponse<CertificationResponse> search(String query, CertificationStatus status, VerificationStatus verification, ComplianceStatus compliance, String expiryBucket, int page, int size, String sort);
    CertificationResponse revoke(UUID id, String reason, RequestActor actor);
    CertificationResponse updateVerification(UUID id, VerificationStatus status, RequestActor actor);
    LegacySyncResponse syncFromM1(RequestActor actor);
}
