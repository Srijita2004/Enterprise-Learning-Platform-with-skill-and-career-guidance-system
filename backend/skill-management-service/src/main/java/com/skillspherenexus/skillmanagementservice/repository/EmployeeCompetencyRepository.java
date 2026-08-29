package com.skillspherenexus.skillmanagementservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillspherenexus.skillmanagementservice.entity.EmployeeCompetency;

public interface EmployeeCompetencyRepository extends JpaRepository<EmployeeCompetency, Integer> {

    List<EmployeeCompetency> findByEmployeeId(Integer employeeId);

    Optional<EmployeeCompetency> findByEmployeeIdAndCompetency_CompetencyId(
            Integer employeeId,
            Integer competencyId
    );
}