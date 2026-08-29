package com.skillspherenexus.certificationmanagementservice.service;

import com.skillspherenexus.certificationmanagementservice.dto.*;
import com.skillspherenexus.certificationmanagementservice.entity.CertificationRecord;
import com.skillspherenexus.certificationmanagementservice.enums.*;
import com.skillspherenexus.certificationmanagementservice.security.RequestActor;
import java.util.*;

public interface ReportService {
    ReportSummaryResponse summary();
    String certificationsCsv();
    String expiringCsv(int days);
}
