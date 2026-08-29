package com.skillspherenexus.skillmanagementservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.security.access.prepost.PreAuthorize;
import com.skillspherenexus.skillmanagementservice.dto.EmployeeRequestDTO;
import com.skillspherenexus.skillmanagementservice.dto.EmployeeResponseDTO;
import com.skillspherenexus.skillmanagementservice.service.EmployeeService;

@RestController
@RequestMapping("/api/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PreAuthorize("hasAnyRole('ADMIN','HR','EMPLOYEE')")
    @PostMapping
    public EmployeeResponseDTO saveEmployee(@RequestBody EmployeeRequestDTO request) {
        return employeeService.saveEmployee(request);
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR','EMPLOYEE','LEARNER')")
    @GetMapping
    public List<EmployeeResponseDTO> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR','EMPLOYEE','LEARNER')")
    @GetMapping("/{id}")
    public EmployeeResponseDTO getEmployeeById(@PathVariable Integer id) {
        return employeeService.getEmployeeById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PutMapping("/{id}")
    public EmployeeResponseDTO updateEmployee(@PathVariable Integer id,
                                              @RequestBody EmployeeRequestDTO request) {
        return employeeService.updateEmployee(id, request);
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @DeleteMapping("/{id}")
    public String deleteEmployee(@PathVariable Integer id) {
        employeeService.deleteEmployee(id);
        return "Employee deleted successfully";
    }
}