package com.skillspherenexus.certificationmanagementservice.dto;
import java.time.LocalDateTime;
public record BulkEvaluationResponse(int processedRecords, int changedRecords, int notificationsCreated, LocalDateTime evaluatedAt) {}
