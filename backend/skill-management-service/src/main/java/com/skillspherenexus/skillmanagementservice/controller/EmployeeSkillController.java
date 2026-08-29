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
import com.skillspherenexus.skillmanagementservice.dto.EmployeeSkillRequestDTO;
import com.skillspherenexus.skillmanagementservice.dto.EmployeeSkillResponseDTO;
import com.skillspherenexus.skillmanagementservice.service.EmployeeSkillService;

@RestController
@RequestMapping("/api/employeeSkills")
public class EmployeeSkillController {

    @Autowired
    private EmployeeSkillService employeeSkillService;

    @PreAuthorize("hasAnyRole('ADMIN','HR','EMPLOYEE')")
    @PostMapping
    public EmployeeSkillResponseDTO saveEmployeeSkill(@RequestBody EmployeeSkillRequestDTO request) {
        return employeeSkillService.saveEmployeeSkill(request);
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR','EMPLOYEE')")
    @GetMapping
    public List<EmployeeSkillResponseDTO> getAllEmployeeSkills() {
        return employeeSkillService.getAllEmployeeSkills();
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR','EMPLOYEE')")
    @GetMapping("/{id}")
    public EmployeeSkillResponseDTO getEmployeeSkillById(@PathVariable Integer id) {
        return employeeSkillService.getEmployeeSkillById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PutMapping("/{id}")
    public EmployeeSkillResponseDTO updateEmployeeSkill(@PathVariable Integer id,
                                                        @RequestBody EmployeeSkillRequestDTO request) {
        return employeeSkillService.updateEmployeeSkill(id, request);
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @DeleteMapping("/{id}")
    public String deleteEmployeeSkill(@PathVariable Integer id) {
        employeeSkillService.deleteEmployeeSkill(id);
        return "Employee Skill deleted successfully";
    }
}