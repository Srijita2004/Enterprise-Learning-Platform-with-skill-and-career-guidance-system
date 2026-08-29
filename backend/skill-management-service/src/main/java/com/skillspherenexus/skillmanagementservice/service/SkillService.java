package com.skillspherenexus.skillmanagementservice.service;

import java.util.List;

import com.skillspherenexus.skillmanagementservice.dto.SkillRequestDTO;
import com.skillspherenexus.skillmanagementservice.dto.SkillResponseDTO;

public interface SkillService {

    SkillResponseDTO saveSkill(SkillRequestDTO request);

    List<SkillResponseDTO> getAllSkills();

    SkillResponseDTO getSkillById(Integer skillId);

    SkillResponseDTO updateSkill(Integer skillId, SkillRequestDTO request);

    void deleteSkill(Integer skillId);
}