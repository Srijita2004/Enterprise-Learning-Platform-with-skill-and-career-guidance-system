package com.skillspherenexus.careerservice.service;

import com.skillspherenexus.careerservice.dto.SkillGapDTO;
import java.util.List;

public interface SkillGapAnalysisService {
    
    List<SkillGapDTO> analyzeSkillGapsForCareerPlan(Long careerPlanId);
    
    SkillGapDTO createSkillGap(Long careerPlanId, SkillGapDTO skillGapDTO);
    
    SkillGapDTO updateSkillGap(Long gapId, SkillGapDTO skillGapDTO);
    
    SkillGapDTO getSkillGap(Long gapId);
    
    List<SkillGapDTO> getUnfilledGapsByCareerPlan(Long careerPlanId);
    
    List<SkillGapDTO> getFilledGapsByCareerPlan(Long careerPlanId);
    
    SkillGapDTO markSkillGapFilled(Long gapId);
    
    Integer getTotalSkillGapCount(Long careerPlanId);
    
    void deleteSkillGap(Long gapId);
}
