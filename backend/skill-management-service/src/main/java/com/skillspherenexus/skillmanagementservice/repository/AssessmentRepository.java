package com.skillspherenexus.skillmanagementservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillspherenexus.skillmanagementservice.entity.Assessment;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
}

