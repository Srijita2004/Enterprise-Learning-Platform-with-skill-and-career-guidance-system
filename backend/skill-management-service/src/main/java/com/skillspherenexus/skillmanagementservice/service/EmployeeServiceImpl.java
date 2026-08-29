package com.skillspherenexus.skillmanagementservice.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.skillspherenexus.skillmanagementservice.dto.EmployeeRequestDTO;
import com.skillspherenexus.skillmanagementservice.dto.EmployeeResponseDTO;
import com.skillspherenexus.skillmanagementservice.entity.Employee;
import com.skillspherenexus.skillmanagementservice.event.EmployeeCreatedEvent;
import com.skillspherenexus.skillmanagementservice.kafka.SkillEventProducer;
import com.skillspherenexus.skillmanagementservice.repository.EmployeeRepository;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private SkillEventProducer skillEventProducer;

    @Override
    public EmployeeResponseDTO saveEmployee(EmployeeRequestDTO request) {
        Employee employee = new Employee();
        employee.setEmployeeName(request.getEmployeeName());
        employee.setDesignation(request.getDesignation());
        employee.setSalary(request.getSalary());

        Employee saved = employeeRepository.save(employee);

        skillEventProducer.publishEmployeeCreated(
                new EmployeeCreatedEvent(saved.getEmployeeId(), saved.getEmployeeName(), saved.getDesignation()));

        return convertToResponse(saved);
    }

    @Override
    public List<EmployeeResponseDTO> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EmployeeResponseDTO getEmployeeById(Integer employeeId) {
        return employeeRepository.findById(employeeId)
                .map(this::convertToResponse)
                .orElse(null);
    }

    @Override
    public EmployeeResponseDTO updateEmployee(Integer employeeId, EmployeeRequestDTO request) {

        Employee existingEmployee = employeeRepository.findById(employeeId).orElse(null);

        if (existingEmployee != null) {
            existingEmployee.setEmployeeName(request.getEmployeeName());
            existingEmployee.setDesignation(request.getDesignation());
            existingEmployee.setSalary(request.getSalary());

            return convertToResponse(employeeRepository.save(existingEmployee));
        }

        logger.warn("Attempted update on nonexistent employee: employeeId={}", employeeId);
        return null;
    }

    @Override
    public void deleteEmployee(Integer employeeId) {
        employeeRepository.deleteById(employeeId);
    }

    private EmployeeResponseDTO convertToResponse(Employee employee) {
        EmployeeResponseDTO response = new EmployeeResponseDTO();
        response.setEmployeeId(employee.getEmployeeId());
        response.setEmployeeName(employee.getEmployeeName());
        response.setDesignation(employee.getDesignation());
        response.setSalary(employee.getSalary());
        return response;
    }
}