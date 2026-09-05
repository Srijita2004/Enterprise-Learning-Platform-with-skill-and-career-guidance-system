package com.skillspherenexus.careerservice.repository;

import com.skillspherenexus.careerservice.entity.JobNomination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobNominationRepository extends JpaRepository<JobNomination, Long> {
    List<JobNomination> findByEmployeeId(Long employeeId);
    List<JobNomination> findByJobId(Long jobId);
    Optional<JobNomination> findByJobIdAndEmployeeId(Long jobId, Long employeeId);
    boolean existsByJobIdAndEmployeeId(Long jobId, Long employeeId);
}
