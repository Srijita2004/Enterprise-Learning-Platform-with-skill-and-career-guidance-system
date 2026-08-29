package com.skillspherenexus.skillmanagementservice.service;

import java.util.List;

import com.skillspherenexus.skillmanagementservice.dto.CompetencyFrameworkRequestDTO;
import com.skillspherenexus.skillmanagementservice.dto.CompetencyFrameworkResponseDTO;
import com.skillspherenexus.skillmanagementservice.dto.CompetencyRequestDTO;
import com.skillspherenexus.skillmanagementservice.dto.CompetencyResponseDTO;
import com.skillspherenexus.skillmanagementservice.dto.EmployeeCompetencyRequestDTO;
import com.skillspherenexus.skillmanagementservice.dto.EmployeeCompetencyResponseDTO;
import com.skillspherenexus.skillmanagementservice.dto.GapResult;

public interface CompetencyService {

    CompetencyResponseDTO create(CompetencyRequestDTO request);

    List<CompetencyResponseDTO> getAll();

    CompetencyResponseDTO getById(Integer id);

    CompetencyResponseDTO update(Integer id, CompetencyRequestDTO request);

    void delete(Integer id);

    CompetencyFrameworkResponseDTO defineFrameworkRequirement(CompetencyFrameworkRequestDTO request);

    List<CompetencyFrameworkResponseDTO> getFrameworkForRole(String role);

    EmployeeCompetencyResponseDTO recordEmployeeLevel(EmployeeCompetencyRequestDTO request);

    List<GapResult> analyzeGap(Integer employeeId, String targetRole);

}