package com.skillspherenexus.skillmanagementservice.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.skillspherenexus.skillmanagementservice.dto.SkillRequestDTO;
import com.skillspherenexus.skillmanagementservice.dto.SkillResponseDTO;
import com.skillspherenexus.skillmanagementservice.entity.Skill;
import com.skillspherenexus.skillmanagementservice.repository.SkillRepository;

@Service
public class SkillServiceImpl implements SkillService {

    private static final Logger logger = LoggerFactory.getLogger(SkillServiceImpl.class);

    @Autowired
    private SkillRepository skillRepository;

    @Override
    public SkillResponseDTO saveSkill(SkillRequestDTO request) {
        Skill skill = new Skill();
        skill.setSkillName(request.getSkillName());
        skill.setCategory(request.getCategory());
        skill.setDescription(request.getDescription());

        return convertToResponse(skillRepository.save(skill));
    }

    @Override
    public List<SkillResponseDTO> getAllSkills() {
        return skillRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SkillResponseDTO getSkillById(Integer skillId) {
        return skillRepository.findById(skillId)
                .map(this::convertToResponse)
                .orElse(null);
    }

    @Override
    public SkillResponseDTO updateSkill(Integer skillId, SkillRequestDTO request) {

        Skill existingSkill = skillRepository.findById(skillId)
                .orElseThrow(() -> {
                    logger.warn("Skill not found: skillId={}", skillId);
                    return new RuntimeException("Skill not found");
                });

        existingSkill.setSkillName(request.getSkillName());
        existingSkill.setCategory(request.getCategory());
        existingSkill.setDescription(request.getDescription());

        return convertToResponse(skillRepository.save(existingSkill));
    }

    @Override
    public void deleteSkill(Integer skillId) {
        skillRepository.deleteById(skillId);
    }

    private SkillResponseDTO convertToResponse(Skill skill) {
        SkillResponseDTO response = new SkillResponseDTO();
        response.setSkillId(skill.getSkillId());
        response.setSkillName(skill.getSkillName());
        response.setCategory(skill.getCategory());
        response.setDescription(skill.getDescription());
        return response;
    }
}