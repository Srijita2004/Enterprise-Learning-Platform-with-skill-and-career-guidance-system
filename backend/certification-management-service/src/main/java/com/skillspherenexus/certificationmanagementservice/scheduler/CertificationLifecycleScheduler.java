package com.skillspherenexus.certificationmanagementservice.scheduler;

import com.skillspherenexus.certificationmanagementservice.security.RequestActor;
import com.skillspherenexus.certificationmanagementservice.service.ExpiryTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component @RequiredArgsConstructor
public class CertificationLifecycleScheduler {
    private final ExpiryTrackingService expiryTrackingService;
    @Scheduled(cron = "${certification.scheduler.cron:0 5 0 * * *}")
    public void evaluateCertificationLifecycle(){ expiryTrackingService.evaluateAll(new RequestActor("scheduler", "SYSTEM")); }
}
