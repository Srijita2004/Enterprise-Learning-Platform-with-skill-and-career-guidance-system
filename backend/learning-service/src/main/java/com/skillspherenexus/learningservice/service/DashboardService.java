package com.skillspherenexus.learningservice.service;

import com.skillspherenexus.learningservice.dto.HrDashboardResponseDTO;
import com.skillspherenexus.learningservice.dto.LearnerDashboardResponseDTO;

import java.util.UUID;

public interface DashboardService {

    LearnerDashboardResponseDTO getLearnerDashboard(
            UUID learnerId
    );

    HrDashboardResponseDTO getHrDashboard();
}