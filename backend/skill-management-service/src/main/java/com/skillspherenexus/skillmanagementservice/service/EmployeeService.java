package com.skillspherenexus.skillmanagementservice.service;

import java.util.List;

import com.skillspherenexus.skillmanagementservice.dto.EmployeeRequestDTO;
import com.skillspherenexus.skillmanagementservice.dto.EmployeeResponseDTO;

public interface EmployeeService {

    EmployeeResponseDTO saveEmployee(EmployeeRequestDTO request);

    List<EmployeeResponseDTO> getAllEmployees();

    EmployeeResponseDTO getEmployeeById(Integer employeeId);

    EmployeeResponseDTO updateEmployee(Integer employeeId, EmployeeRequestDTO request);

    void deleteEmployee(Integer employeeId);
}