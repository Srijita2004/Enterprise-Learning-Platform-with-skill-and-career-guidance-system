package com.skillspherenexus.careerservice.repository;

import com.skillspherenexus.careerservice.entity.JobOpportunity;
import com.skillspherenexus.careerservice.entity.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobOpportunityRepository extends JpaRepository<JobOpportunity, Long> {
    
    List<JobOpportunity> findByStatus(JobStatus status);
    
    List<JobOpportunity> findByDepartment(String department);
    
    @Query("SELECT jo FROM JobOpportunity jo WHERE jo.status = :status ORDER BY jo.postedDate DESC")
    List<JobOpportunity> findOpenJobsSorted(@Param("status") JobStatus status);
    
    @Query("SELECT jo FROM JobOpportunity jo WHERE jo.location = :location AND jo.status = :status")
    List<JobOpportunity> findByLocationAndStatus(@Param("location") String location, @Param("status") JobStatus status);
    
    @Query("SELECT COUNT(jo) FROM JobOpportunity jo WHERE jo.status = :status")
    Long countByStatus(@Param("status") JobStatus status);
}
