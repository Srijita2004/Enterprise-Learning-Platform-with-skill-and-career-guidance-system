package com.skillspherenexus.careerservice.service;

import com.skillspherenexus.careerservice.dto.CandidateMatchDTO;
import com.skillspherenexus.careerservice.dto.JobOpportunityDTO;
import java.util.List;

public interface JobPortalService {
    
    JobOpportunityDTO createJobOpportunity(JobOpportunityDTO jobDTO);
    
    JobOpportunityDTO updateJobOpportunity(Long jobId, JobOpportunityDTO jobDTO);
    
    JobOpportunityDTO getJobOpportunity(Long jobId);
    
    List<JobOpportunityDTO> getAllOpenJobs();

    List<JobOpportunityDTO> getAllJobs();
    
    List<JobOpportunityDTO> getJobsByDepartment(String department);
    
    List<JobOpportunityDTO> getJobsByLocation(String location);
    
    List<JobOpportunityDTO> findMatchingJobsForCareerPlan(Long careerPlanId);
    
    List<JobOpportunityDTO> findMatchingJobsForEmployee(Long employeeId);
    
    Integer calculateJobMatchScore(Long careerPlanId, Long jobId);

    List<CandidateMatchDTO> findMatchingCandidatesForJob(Long jobId);
    
    void closeJobOpportunity(Long jobId);

    JobOpportunityDTO toggleJobStatus(Long jobId, String status);
    
    void deleteJobOpportunity(Long jobId);
}

