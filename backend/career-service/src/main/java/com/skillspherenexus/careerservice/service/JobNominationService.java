package com.skillspherenexus.careerservice.service;

import com.skillspherenexus.careerservice.dto.JobNominationDTO;
import java.util.List;

public interface JobNominationService {
    JobNominationDTO createNomination(JobNominationDTO dto, String nominatedByUsername);
    List<JobNominationDTO> getNominationsByEmployee(Long employeeId);
    List<JobNominationDTO> getNominationsByJob(Long jobId);
    JobNominationDTO updateNominationStatus(Long nominationId, String status);
    boolean isEmployeeNominatedForJob(Long jobId, Long employeeId);
}
