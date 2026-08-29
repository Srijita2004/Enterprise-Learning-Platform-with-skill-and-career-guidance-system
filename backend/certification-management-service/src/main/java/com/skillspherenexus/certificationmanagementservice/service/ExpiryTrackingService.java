package com.skillspherenexus.certificationmanagementservice.service;

import com.skillspherenexus.certificationmanagementservice.dto.*;
import com.skillspherenexus.certificationmanagementservice.entity.CertificationRecord;
import com.skillspherenexus.certificationmanagementservice.enums.*;
import com.skillspherenexus.certificationmanagementservice.security.RequestActor;
import java.util.*;

public interface ExpiryTrackingService {
    CertificationRecord evaluate(CertificationRecord record);
    List<CertificationResponse> expiringWithin(int days);
    List<CertificationResponse> expired();
    BulkEvaluationResponse evaluateAll(RequestActor actor);
    List<ExpiryBucketResponse> distribution();
}
