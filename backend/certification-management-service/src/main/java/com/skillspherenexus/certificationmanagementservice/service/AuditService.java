package com.skillspherenexus.certificationmanagementservice.service;

import com.skillspherenexus.certificationmanagementservice.dto.*;
import com.skillspherenexus.certificationmanagementservice.entity.CertificationRecord;
import com.skillspherenexus.certificationmanagementservice.enums.*;
import com.skillspherenexus.certificationmanagementservice.security.RequestActor;
import java.util.*;

public interface AuditService {
    void record(UUID certificationId, AuditAction action, RequestActor actor, String details, Object beforeState, Object afterState);
    List<AuditLogResponse> recent();
    List<AuditLogResponse> byCertification(UUID certificationId);
}
