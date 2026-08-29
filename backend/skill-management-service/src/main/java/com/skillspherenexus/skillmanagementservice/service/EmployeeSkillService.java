package com.skillspherenexus.skillmanagementservice.service;

import java.util.List;

import com.skillspherenexus.skillmanagementservice.dto.EmployeeSkillRequestDTO;
import com.skillspherenexus.skillmanagementservice.dto.EmployeeSkillResponseDTO;

public interface EmployeeSkillService {

    EmployeeSkillResponseDTO saveEmployeeSkill(EmployeeSkillRequestDTO request);

    List<EmployeeSkillResponseDTO> getAllEmployeeSkills();

    EmployeeSkillResponseDTO getEmployeeSkillById(Integer employeeSkillId);

    EmployeeSkillResponseDTO updateEmployeeSkill(Integer employeeSkillId, EmployeeSkillRequestDTO request);

    void deleteEmployeeSkill(Integer employeeSkillId);
}