package com.skillspherenexus.certificationmanagementservice.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.UUID;

public record RenewalCreateRequest(
        @NotNull UUID certificationId,
        @NotNull @Future LocalDate proposedExpiryDate,
        @NotBlank @Size(max = 600) String justification
) {}
