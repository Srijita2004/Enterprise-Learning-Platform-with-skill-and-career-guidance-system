package com.skillspherenexus.skillmanagementservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillspherenexus.skillmanagementservice.entity.EmployeeSkill;

public interface EmployeeSkillRepository extends JpaRepository<EmployeeSkill, Integer> {

}