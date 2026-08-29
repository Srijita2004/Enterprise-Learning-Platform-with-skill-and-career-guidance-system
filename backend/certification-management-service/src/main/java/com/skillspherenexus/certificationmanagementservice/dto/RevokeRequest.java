package com.skillspherenexus.certificationmanagementservice.dto;
import jakarta.validation.constraints.*;
public record RevokeRequest(@NotBlank @Size(max = 600) String reason) {}
