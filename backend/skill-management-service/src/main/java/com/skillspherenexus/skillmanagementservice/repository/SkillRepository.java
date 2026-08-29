package com.skillspherenexus.skillmanagementservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillspherenexus.skillmanagementservice.entity.Skill;

public interface SkillRepository extends JpaRepository<Skill, Integer> {

}