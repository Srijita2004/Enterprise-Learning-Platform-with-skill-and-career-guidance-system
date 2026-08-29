package com.skillspherenexus.skillmanagementservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillspherenexus.skillmanagementservice.entity.CompetencyFramework;

public interface CompetencyFrameworkRepository extends JpaRepository<CompetencyFramework, Integer> {

    List<CompetencyFramework> findByRole(String role);

    boolean existsByRoleAndCompetency_CompetencyId(String role, Integer competencyId);

}
