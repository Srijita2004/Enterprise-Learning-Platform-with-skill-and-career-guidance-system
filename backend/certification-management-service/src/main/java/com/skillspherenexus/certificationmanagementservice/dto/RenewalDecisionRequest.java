package com.skillspherenexus.certificationmanagementservice.dto;

import jakarta.validation.constraints.Size;
public record RenewalDecisionRequest(@Size(max = 600) String decisionNote) {}
