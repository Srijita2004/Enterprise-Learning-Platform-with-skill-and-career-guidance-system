package com.skillspherenexus.skillmanagementservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillspherenexus.skillmanagementservice.entity.Competency;

public interface CompetencyRepository extends JpaRepository<Competency, Integer> {
}
