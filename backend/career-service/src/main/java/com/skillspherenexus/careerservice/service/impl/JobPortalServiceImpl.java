package com.skillspherenexus.careerservice.service.impl;

import com.skillspherenexus.careerservice.dto.JobOpportunityDTO;
import com.skillspherenexus.careerservice.entity.JobOpportunity;
import com.skillspherenexus.careerservice.entity.JobStatus;
import com.skillspherenexus.careerservice.entity.CareerPlan;
import com.skillspherenexus.careerservice.entity.SkillGap;
import com.skillspherenexus.careerservice.repository.JobOpportunityRepository;
import com.skillspherenexus.careerservice.repository.CareerPlanRepository;
import com.skillspherenexus.careerservice.repository.SkillGapRepository;
import com.skillspherenexus.careerservice.service.JobPortalService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class JobPortalServiceImpl implements JobPortalService {

    private final JobOpportunityRepository jobRepository;
    private final CareerPlanRepository careerPlanRepository;
    private final SkillGapRepository skillGapRepository;
    private final ModelMapper modelMapper;

    @Override
    public JobOpportunityDTO createJobOpportunity(JobOpportunityDTO jobDTO) {
        JobOpportunity job = modelMapper.map(jobDTO, JobOpportunity.class);
        job.setStatus(JobStatus.OPEN);
        job.setPostedDate(LocalDateTime.now());
        JobOpportunity saved = jobRepository.save(job);
        return modelMapper.map(saved, JobOpportunityDTO.class);
    }

    @Override
    public JobOpportunityDTO updateJobOpportunity(Long jobId, JobOpportunityDTO jobDTO) {
        JobOpportunity existing = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));
        
        modelMapper.map(jobDTO, existing);
        existing.setUpdatedAt(LocalDateTime.now());
        JobOpportunity updated = jobRepository.save(existing);
        return modelMapper.map(updated, JobOpportunityDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public JobOpportunityDTO getJobOpportunity(Long jobId) {
        JobOpportunity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));
        return modelMapper.map(job, JobOpportunityDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobOpportunityDTO> getAllOpenJobs() {
        return jobRepository.findByStatus(JobStatus.OPEN)
                .stream()
                .map(j -> modelMapper.map(j, JobOpportunityDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobOpportunityDTO> getJobsByDepartment(String department) {
        return jobRepository.findByDepartment(department)
                .stream()
                .map(j -> modelMapper.map(j, JobOpportunityDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobOpportunityDTO> getJobsByLocation(String location) {
        return jobRepository.findByLocationAndStatus(location, JobStatus.OPEN)
                .stream()
                .map(j -> modelMapper.map(j, JobOpportunityDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobOpportunityDTO> findMatchingJobsForCareerPlan(Long careerPlanId) {
        CareerPlan careerPlan = careerPlanRepository.findById(careerPlanId)
                .orElseThrow(() -> new IllegalArgumentException("Career plan not found: " + careerPlanId));
        
        return jobRepository.findByStatus(JobStatus.OPEN)
                .stream()
                .filter(job -> isJobMatch(careerPlan, job))
                .collect(Collectors.toList())
                .stream()
                .map(j -> modelMapper.map(j, JobOpportunityDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Integer calculateJobMatchScore(Long careerPlanId, Long jobId) {
        CareerPlan careerPlan = careerPlanRepository.findById(careerPlanId)
                .orElseThrow(() -> new IllegalArgumentException("Career plan not found: " + careerPlanId));
        
        JobOpportunity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));
        
        int score = calculateMatchScore(careerPlan, job);
        return Math.min(100, Math.max(0, score));
    }

    @Override
    public void closeJobOpportunity(Long jobId) {
        JobOpportunity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));
        
        job.setStatus(JobStatus.CLOSED);
        job.setClosedDate(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());
        jobRepository.save(job);
    }

    @Override
    public void deleteJobOpportunity(Long jobId) {
        JobOpportunity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));
        jobRepository.delete(job);
    }

    private boolean isJobMatch(CareerPlan careerPlan, JobOpportunity job) {
        int score = calculateMatchScore(careerPlan, job);
        return score >= 60; // 60% threshold for match
    }

    private int calculateMatchScore(CareerPlan careerPlan, JobOpportunity job) {
        int score = 0;
        
        // Check if target role matches or is close
        if (job.getJobTitle().toLowerCase().contains(careerPlan.getTargetRole().toLowerCase())) {
            score += 30;
        } else if (job.getJobTitle().toLowerCase().contains("technical") && careerPlan.getTargetRole().toLowerCase().contains("lead")) {
            score += 20;
        }
        
        // Check experience requirement
        if (job.getRequiredExperienceYears() <= 15) { // Assume mid-career
            score += 20;
        }
        
        // Check skill gaps (fewer gaps = better match)
        List<SkillGap> gaps = skillGapRepository.findByCareerPlanId(careerPlan.getId());
        if (gaps.isEmpty()) {
            score += 30;
        } else if (gaps.size() <= 3) {
            score += 20;
        } else {
            score += 10;
        }
        
        // Check salary alignment (basic check)
        if (job.getSalary() > 100000) {
            score += 10;
        }
        
        return Math.min(100, score);
    }
}
