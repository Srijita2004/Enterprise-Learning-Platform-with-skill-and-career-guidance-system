package com.skillspherenexus.certificationmanagementservice.dto;
import com.skillspherenexus.certificationmanagementservice.enums.VerificationStatus;
import jakarta.validation.constraints.NotNull;
public record VerificationUpdateRequest(@NotNull VerificationStatus status) {}
