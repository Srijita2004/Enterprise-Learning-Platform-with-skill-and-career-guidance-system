package com.skillspherenexus.careerservice.service;

import com.skillspherenexus.careerservice.dto.CareerPlanDTO;
import com.skillspherenexus.careerservice.dto.PromotionCriteriaDTO;
import com.skillspherenexus.careerservice.dto.SkillGapDTO;

import java.util.List;
import java.util.Optional;

public interface CareerPlanService {
    
    CareerPlanDTO createCareerPlan(CareerPlanDTO careerPlanDTO);
    
    CareerPlanDTO updateCareerPlan(Long planId, CareerPlanDTO careerPlanDTO);
    
    CareerPlanDTO getCareerPlan(Long planId);
    
    CareerPlanDTO getCareerPlanByEmployeeId(Long employeeId);
    
    List<CareerPlanDTO> getAllActiveCareerPlans();
    
    List<CareerPlanDTO> getCareerPlansByMentor(String mentorId);
    
    CareerPlanDTO togglePromotionCriteria(Long planId, Long criteriaId);
    
    CareerPlanDTO updateMentor(Long planId, String mentorId, String mentorName);
    
    Integer calculateProgressPercentage(Long planId);
    
    void deleteCareerPlan(Long planId);
}
