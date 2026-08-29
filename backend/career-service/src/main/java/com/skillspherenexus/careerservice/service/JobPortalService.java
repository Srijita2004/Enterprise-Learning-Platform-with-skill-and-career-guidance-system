package com.skillspherenexus.careerservice.service;

import com.skillspherenexus.careerservice.dto.JobOpportunityDTO;
import java.util.List;

public interface JobPortalService {
    
    JobOpportunityDTO createJobOpportunity(JobOpportunityDTO jobDTO);
    
    JobOpportunityDTO updateJobOpportunity(Long jobId, JobOpportunityDTO jobDTO);
    
    JobOpportunityDTO getJobOpportunity(Long jobId);
    
    List<JobOpportunityDTO> getAllOpenJobs();
    
    List<JobOpportunityDTO> getJobsByDepartment(String department);
    
    List<JobOpportunityDTO> getJobsByLocation(String location);
    
    List<JobOpportunityDTO> findMatchingJobsForCareerPlan(Long careerPlanId);
    
    Integer calculateJobMatchScore(Long careerPlanId, Long jobId);
    
    void closeJobOpportunity(Long jobId);
    
    void deleteJobOpportunity(Long jobId);
}
