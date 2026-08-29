package com.skillspherenexus.skillmanagementservice.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.skillspherenexus.skillmanagementservice.dto.CompetencyFrameworkRequestDTO;
import com.skillspherenexus.skillmanagementservice.dto.CompetencyFrameworkResponseDTO;
import com.skillspherenexus.skillmanagementservice.dto.CompetencyRequestDTO;
import com.skillspherenexus.skillmanagementservice.dto.CompetencyResponseDTO;
import com.skillspherenexus.skillmanagementservice.dto.EmployeeCompetencyRequestDTO;
import com.skillspherenexus.skillmanagementservice.dto.EmployeeCompetencyResponseDTO;
import com.skillspherenexus.skillmanagementservice.dto.GapResult;
import com.skillspherenexus.skillmanagementservice.entity.Competency;
import com.skillspherenexus.skillmanagementservice.entity.CompetencyFramework;
import com.skillspherenexus.skillmanagementservice.entity.EmployeeCompetency;
import com.skillspherenexus.skillmanagementservice.repository.CompetencyFrameworkRepository;
import com.skillspherenexus.skillmanagementservice.repository.CompetencyRepository;
import com.skillspherenexus.skillmanagementservice.repository.EmployeeCompetencyRepository;

@Service
public class CompetencyServiceImpl implements CompetencyService {

    private static final Logger logger = LoggerFactory.getLogger(CompetencyServiceImpl.class);

    private final CompetencyRepository competencyRepository;
    private final CompetencyFrameworkRepository frameworkRepository;
    private final EmployeeCompetencyRepository employeeCompetencyRepository;

    public CompetencyServiceImpl(CompetencyRepository competencyRepository,
                                 CompetencyFrameworkRepository frameworkRepository,
                                 EmployeeCompetencyRepository employeeCompetencyRepository) {
        this.competencyRepository = competencyRepository;
        this.frameworkRepository = frameworkRepository;
        this.employeeCompetencyRepository = employeeCompetencyRepository;
    }

    @Override
    public CompetencyResponseDTO create(CompetencyRequestDTO request) {
        Competency competency = new Competency();
        competency.setName(request.getName());
        competency.setCategory(request.getCategory());
        competency.setDescription(request.getDescription());
        competency.setMaxLevel(request.getMaxLevel());

        return convertToResponse(competencyRepository.save(competency));
    }

    @Override
    public List<CompetencyResponseDTO> getAll() {
        return competencyRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CompetencyResponseDTO getById(Integer id) {
        return competencyRepository.findById(id)
                .map(this::convertToResponse)
                .orElseThrow(() -> new RuntimeException("Competency not found: " + id));
    }

    @Override
    public CompetencyResponseDTO update(Integer id, CompetencyRequestDTO request) {

        Competency existing = competencyRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Attempted to update nonexistent competency with id={}", id);
                    return new RuntimeException("Competency not found: " + id);
                });

        existing.setName(request.getName());
        existing.setCategory(request.getCategory());
        existing.setDescription(request.getDescription());
        existing.setMaxLevel(request.getMaxLevel());

        return convertToResponse(competencyRepository.save(existing));
    }

    @Override
    public void delete(Integer id) {
        Competency existing = competencyRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Attempted to delete nonexistent competency with id={}", id);
                    return new RuntimeException("Competency not found: " + id);
                });
        competencyRepository.delete(existing);
    }

    @Override
    public CompetencyFrameworkResponseDTO defineFrameworkRequirement(
            CompetencyFrameworkRequestDTO request) {

        if (frameworkRepository.existsByRoleAndCompetency_CompetencyId(
                request.getRole(),
                request.getCompetencyId())) {

            logger.info("Competency {} already defined for role {}", request.getCompetencyId(), request.getRole());
            throw new IllegalArgumentException(
                    "This competency is already defined for the selected role.");
        }

        Competency competency = competencyRepository.findById(request.getCompetencyId())
                .orElseThrow(() -> {
                    logger.warn("Competency not found for framework request, competencyId={}", request.getCompetencyId());
                    return new RuntimeException("Competency not found");
                });

        CompetencyFramework framework = new CompetencyFramework();
        framework.setRole(request.getRole());
        framework.setCompetency(competency);
        framework.setRequiredLevel(request.getRequiredLevel());

        return convertToResponse(frameworkRepository.save(framework));
    }   

    @Override
    public List<CompetencyFrameworkResponseDTO> getFrameworkForRole(String role) {
        return frameworkRepository.findByRole(role).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EmployeeCompetencyResponseDTO recordEmployeeLevel(EmployeeCompetencyRequestDTO request) {

        Integer employeeId = request.getEmployeeId();
        Integer competencyId = request.getCompetencyId();

        EmployeeCompetency existing = employeeCompetencyRepository
                .findByEmployeeIdAndCompetency_CompetencyId(employeeId, competencyId)
                .orElse(new EmployeeCompetency());

        existing.setEmployeeId(employeeId);
        existing.setCompetency(competencyRepository.findById(competencyId)
                .orElseThrow(() -> {
                    logger.warn("Competency not found while recording employee level, competencyId={}", competencyId);
                    return new RuntimeException("Competency not found: " + competencyId);
                }));
        existing.setCurrentLevel(request.getCurrentLevel());

        return convertToResponse(employeeCompetencyRepository.save(existing));
    }

    @Override
    public List<GapResult> analyzeGap(Integer employeeId, String targetRole) {

        List<CompetencyFramework> requirements = frameworkRepository.findByRole(targetRole);

        if (requirements.isEmpty()) {
            logger.info("No competency framework defined for role={}", targetRole);
        }

        return requirements.stream()
                .map(req -> {
                    Integer competencyId = req.getCompetency().getCompetencyId();

                    int currentLevel = employeeCompetencyRepository
                            .findByEmployeeIdAndCompetency_CompetencyId(employeeId, competencyId)
                            .map(employeeCompetency -> employeeCompetency.getCurrentLevel())
                            .orElse(0);

                    int gap = Math.max(0, req.getRequiredLevel() - currentLevel);

                    return new GapResult(
                            req.getCompetency().getName(),
                            req.getRequiredLevel(),
                            currentLevel,
                            gap
                    );
                })
                .toList();
    }

    private CompetencyResponseDTO convertToResponse(Competency competency) {
        CompetencyResponseDTO response = new CompetencyResponseDTO();
        response.setCompetencyId(competency.getCompetencyId());
        response.setName(competency.getName());
        response.setCategory(competency.getCategory());
        response.setDescription(competency.getDescription());
        response.setMaxLevel(competency.getMaxLevel());
        return response;
    }

    private CompetencyFrameworkResponseDTO convertToResponse(CompetencyFramework framework) {
        CompetencyFrameworkResponseDTO response = new CompetencyFrameworkResponseDTO();
        response.setFrameworkId(framework.getFrameworkId());
        response.setRole(framework.getRole());
        response.setCompetencyId(framework.getCompetency().getCompetencyId());
        response.setRequiredLevel(framework.getRequiredLevel());
        return response;
    }

    private EmployeeCompetencyResponseDTO convertToResponse(EmployeeCompetency employeeCompetency) {
        EmployeeCompetencyResponseDTO response = new EmployeeCompetencyResponseDTO();
        response.setEmployeeCompetencyId(employeeCompetency.getEmployeeCompetencyId());
        response.setEmployeeId(employeeCompetency.getEmployeeId());
        response.setCompetencyId(employeeCompetency.getCompetency().getCompetencyId());
        response.setCurrentLevel(employeeCompetency.getCurrentLevel());
        return response;
    }
}