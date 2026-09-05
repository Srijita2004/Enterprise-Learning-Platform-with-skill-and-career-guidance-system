package com.skillspherenexus.careerservice.service.impl;

import com.skillspherenexus.careerservice.dto.JobNominationDTO;
import com.skillspherenexus.careerservice.entity.*;
import com.skillspherenexus.careerservice.repository.*;
import com.skillspherenexus.careerservice.service.JobNominationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class JobNominationServiceImpl implements JobNominationService {

    private final JobNominationRepository jobNominationRepository;
    private final JobOpportunityRepository jobOpportunityRepository;
    private final CareerPlanRepository careerPlanRepository;
    private final ModelMapper modelMapper;

    @Override
    public JobNominationDTO createNomination(JobNominationDTO dto, String nominatedByUsername) {
        // Validate job exists
        JobOpportunity job = jobOpportunityRepository.findById(dto.getJobId())
                .orElseThrow(() -> new IllegalArgumentException("Job vacancy not found with ID: " + dto.getJobId()));

        // Check for duplicate nomination
        if (jobNominationRepository.existsByJobIdAndEmployeeId(dto.getJobId(), dto.getEmployeeId())) {
            throw new IllegalStateException("Employee #" + dto.getEmployeeId() + " is already nominated for this job: " + job.getJobTitle());
        }

        // Determine employee name if not provided
        String empName = dto.getEmployeeName();
        if (empName == null || empName.isBlank()) {
            empName = careerPlanRepository.findByEmployeeId(dto.getEmployeeId())
                    .map(CareerPlan::getEmployeeName)
                    .orElse("Employee #" + dto.getEmployeeId());
        }

        JobNomination nomination = JobNomination.builder()
                .jobId(job.getId())
                .jobTitle(job.getJobTitle())
                .employeeId(dto.getEmployeeId())
                .employeeName(empName)
                .nominatedBy(nominatedByUsername != null ? nominatedByUsername : "HR_MANAGER")
                .matchScore(dto.getMatchScore() != null ? dto.getMatchScore() : 85)
                .status(NominationStatus.PENDING_REVIEW)
                .notes(dto.getNotes() != null ? dto.getNotes() : "Nominated via Career Analytics Candidate Matcher.")
                .nominationDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        JobNomination saved = jobNominationRepository.save(nomination);
        log.info("HR nominated employee '{}' ({}) for job '{}' (ID: {})",
                saved.getEmployeeName(), saved.getEmployeeId(), saved.getJobTitle(), saved.getJobId());

        return modelMapper.map(saved, JobNominationDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobNominationDTO> getNominationsByEmployee(Long employeeId) {
        return jobNominationRepository.findByEmployeeId(employeeId)
                .stream()
                .map(n -> modelMapper.map(n, JobNominationDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobNominationDTO> getNominationsByJob(Long jobId) {
        return jobNominationRepository.findByJobId(jobId)
                .stream()
                .map(n -> modelMapper.map(n, JobNominationDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public JobNominationDTO updateNominationStatus(Long nominationId, String status) {
        JobNomination nomination = jobNominationRepository.findById(nominationId)
                .orElseThrow(() -> new IllegalArgumentException("Nomination not found: " + nominationId));

        try {
            NominationStatus newStatus = NominationStatus.valueOf(status.toUpperCase());
            nomination.setStatus(newStatus);
            nomination.setUpdatedAt(LocalDateTime.now());
            JobNomination saved = jobNominationRepository.save(nomination);
            return modelMapper.map(saved, JobNominationDTO.class);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid nomination status: " + status);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmployeeNominatedForJob(Long jobId, Long employeeId) {
        return jobNominationRepository.existsByJobIdAndEmployeeId(jobId, employeeId);
    }
}
