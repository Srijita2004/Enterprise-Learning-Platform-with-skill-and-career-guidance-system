package com.skillspherenexus.skillmanagementservice.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.security.access.prepost.PreAuthorize;
import com.skillspherenexus.skillmanagementservice.dto.CompetencyFrameworkRequestDTO;
import com.skillspherenexus.skillmanagementservice.dto.CompetencyFrameworkResponseDTO;
import com.skillspherenexus.skillmanagementservice.dto.CompetencyRequestDTO;
import com.skillspherenexus.skillmanagementservice.dto.CompetencyResponseDTO;
import com.skillspherenexus.skillmanagementservice.dto.EmployeeCompetencyRequestDTO;
import com.skillspherenexus.skillmanagementservice.dto.EmployeeCompetencyResponseDTO;
import com.skillspherenexus.skillmanagementservice.dto.GapResult;
import com.skillspherenexus.skillmanagementservice.service.CompetencyService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/competencies")
public class CompetencyController {

    private final CompetencyService competencyService;

    public CompetencyController(CompetencyService competencyService) {
        this.competencyService = competencyService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PostMapping
    public CompetencyResponseDTO create(@Valid @RequestBody CompetencyRequestDTO request) {
        return competencyService.create(request);
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR','EMPLOYEE','LEARNER')")
    @GetMapping
    public List<CompetencyResponseDTO> getAll() {
        return competencyService.getAll();
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR','EMPLOYEE','LEARNER')")
    @GetMapping("/{id}")
    public CompetencyResponseDTO getById(@PathVariable Integer id) {
        return competencyService.getById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PutMapping("/{id}")
    public CompetencyResponseDTO update(@PathVariable Integer id, @RequestBody CompetencyRequestDTO request) {
        return competencyService.update(id, request);
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        competencyService.delete(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PostMapping("/frameworks")
    public CompetencyFrameworkResponseDTO defineFramework(@RequestBody CompetencyFrameworkRequestDTO request) {
        return competencyService.defineFrameworkRequirement(request);
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR','EMPLOYEE','LEARNER')")
    @GetMapping("/frameworks/role/{role}")
    public ResponseEntity<List<CompetencyFrameworkResponseDTO>> getFrameworkForRole(
            @PathVariable String role) {
        List<CompetencyFrameworkResponseDTO> list =
                competencyService.getFrameworkForRole(role);

        return ResponseEntity.ok(list);
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR','EMPLOYEE')")
    @PostMapping("/employee-levels")
    public EmployeeCompetencyResponseDTO recordEmployeeLevel(@RequestBody EmployeeCompetencyRequestDTO request) {
        return competencyService.recordEmployeeLevel(request);
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR','EMPLOYEE','LEARNER')")
    @GetMapping("/gap-analysis")
    public List<GapResult> getGapAnalysis(@RequestParam Integer employeeId, @RequestParam String role) {
        return competencyService.analyzeGap(employeeId, role);
    }
}
