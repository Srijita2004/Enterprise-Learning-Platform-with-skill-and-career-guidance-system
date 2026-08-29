package com.skillspherenexus.careerservice.service.impl;

import com.skillspherenexus.careerservice.dto.SkillGapDTO;
import com.skillspherenexus.careerservice.entity.*;
import com.skillspherenexus.careerservice.repository.*;
import com.skillspherenexus.careerservice.service.SkillGapAnalysisService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SkillGapAnalysisServiceImpl implements SkillGapAnalysisService {

    private final SkillGapRepository skillGapRepository;
    private final CareerPlanRepository careerPlanRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public List<SkillGapDTO> analyzeSkillGapsForCareerPlan(Long careerPlanId) {
        return skillGapRepository.findByCareerPlanId(careerPlanId)
                .stream()
                .map(sg -> modelMapper.map(sg, SkillGapDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public SkillGapDTO createSkillGap(Long careerPlanId, SkillGapDTO skillGapDTO) {
        CareerPlan careerPlan = careerPlanRepository.findById(careerPlanId)
                .orElseThrow(() -> new IllegalArgumentException("Career plan not found: " + careerPlanId));
        
        SkillGap skillGap = modelMapper.map(skillGapDTO, SkillGap.class);
        skillGap.setCareerPlan(careerPlan);
        if (skillGap.getGapLevel() == null) {
            skillGap.setGapLevel(skillGapDTO.getRequiredLevel() - skillGapDTO.getCurrentLevel());
        }
        SkillGap saved = skillGapRepository.save(skillGap);
        return modelMapper.map(saved, SkillGapDTO.class);
    }

    @Override
    public SkillGapDTO updateSkillGap(Long gapId, SkillGapDTO skillGapDTO) {
        SkillGap existing = skillGapRepository.findById(gapId)
                .orElseThrow(() -> new IllegalArgumentException("Skill gap not found: " + gapId));
        
        modelMapper.map(skillGapDTO, existing);
        existing.setUpdatedAt(LocalDateTime.now());
        SkillGap updated = skillGapRepository.save(existing);
        return modelMapper.map(updated, SkillGapDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public SkillGapDTO getSkillGap(Long gapId) {
        SkillGap skillGap = skillGapRepository.findById(gapId)
                .orElseThrow(() -> new IllegalArgumentException("Skill gap not found: " + gapId));
        return modelMapper.map(skillGap, SkillGapDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillGapDTO> getUnfilledGapsByCareerPlan(Long careerPlanId) {
        return skillGapRepository.findUnfilledGapsByCareerPlan(careerPlanId)
                .stream()
                .map(sg -> modelMapper.map(sg, SkillGapDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillGapDTO> getFilledGapsByCareerPlan(Long careerPlanId) {
        return skillGapRepository.findFilledGapsByCareerPlan(careerPlanId)
                .stream()
                .map(sg -> modelMapper.map(sg, SkillGapDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public SkillGapDTO markSkillGapFilled(Long gapId) {
        SkillGap skillGap = skillGapRepository.findById(gapId)
                .orElseThrow(() -> new IllegalArgumentException("Skill gap not found: " + gapId));
        
        skillGap.setFilledDate(LocalDateTime.now());
        skillGap.setUpdatedAt(LocalDateTime.now());
        SkillGap updated = skillGapRepository.save(skillGap);
        return modelMapper.map(updated, SkillGapDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getTotalSkillGapCount(Long careerPlanId) {
        return Math.toIntExact(skillGapRepository.countGapsByCareerPlan(careerPlanId));
    }

    @Override
    public void deleteSkillGap(Long gapId) {
        SkillGap skillGap = skillGapRepository.findById(gapId)
                .orElseThrow(() -> new IllegalArgumentException("Skill gap not found: " + gapId));
        skillGapRepository.delete(skillGap);
    }
}
