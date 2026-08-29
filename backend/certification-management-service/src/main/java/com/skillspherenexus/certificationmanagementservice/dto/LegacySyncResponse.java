package com.skillspherenexus.certificationmanagementservice.dto;
import java.time.LocalDateTime;
public record LegacySyncResponse(int discovered, int imported, int skippedExisting, int failed, LocalDateTime synchronizedAt) {}
