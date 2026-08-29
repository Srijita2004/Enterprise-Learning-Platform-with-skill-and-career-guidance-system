package com.skillspherenexus.careerservice.service.impl;

import com.skillspherenexus.careerservice.dto.CareerPlanDTO;
import com.skillspherenexus.careerservice.dto.PromotionCriteriaDTO;
import com.skillspherenexus.careerservice.entity.*;
import com.skillspherenexus.careerservice.repository.*;
import com.skillspherenexus.careerservice.service.CareerPlanService;
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
public class CareerPlanServiceImpl implements CareerPlanService {

    private final CareerPlanRepository careerPlanRepository;
    private final PromotionCriteriaRepository promotionCriteriaRepository;
    private final SkillGapRepository skillGapRepository;
    private final ModelMapper modelMapper;

    @Override
    public CareerPlanDTO createCareerPlan(CareerPlanDTO careerPlanDTO) {
        CareerPlan careerPlan = modelMapper.map(careerPlanDTO, CareerPlan.class);
        careerPlan.setProgressPercentage(0);
        CareerPlan saved = careerPlanRepository.save(careerPlan);
        return modelMapper.map(saved, CareerPlanDTO.class);
    }

    @Override
    public CareerPlanDTO updateCareerPlan(Long planId, CareerPlanDTO careerPlanDTO) {
        CareerPlan existing = careerPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Career plan not found: " + planId));
        
        modelMapper.map(careerPlanDTO, existing);
        existing.setUpdatedAt(LocalDateTime.now());
        CareerPlan updated = careerPlanRepository.save(existing);
        return modelMapper.map(updated, CareerPlanDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public CareerPlanDTO getCareerPlan(Long planId) {
        CareerPlan careerPlan = careerPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Career plan not found: " + planId));
        return mapToDTO(careerPlan);
    }

    @Override
    @Transactional(readOnly = true)
    public CareerPlanDTO getCareerPlanByEmployeeId(Long employeeId) {
        CareerPlan careerPlan = careerPlanRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("No career plan found for employee: " + employeeId));
        return mapToDTO(careerPlan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CareerPlanDTO> getAllActiveCareerPlans() {
        return careerPlanRepository.findByStatus(CareerPlanStatus.ACTIVE)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CareerPlanDTO> getCareerPlansByMentor(String mentorId) {
        return careerPlanRepository.findByMentorId(mentorId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CareerPlanDTO togglePromotionCriteria(Long planId, Long criteriaId) {
        CareerPlan careerPlan = careerPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Career plan not found: " + planId));
        
        PromotionCriteria criteria = promotionCriteriaRepository.findById(criteriaId)
                .orElseThrow(() -> new IllegalArgumentException("Criteria not found: " + criteriaId));
        
        criteria.setIsMet(!criteria.getIsMet());
        if (criteria.getIsMet()) {
            criteria.setMetDate(LocalDateTime.now());
        }
        promotionCriteriaRepository.save(criteria);
        
        recalculateProgress(careerPlan);
        CareerPlan updated = careerPlanRepository.save(careerPlan);
        return mapToDTO(updated);
    }

    @Override
    public CareerPlanDTO updateMentor(Long planId, String mentorId, String mentorName) {
        CareerPlan careerPlan = careerPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Career plan not found: " + planId));
        
        careerPlan.setMentorId(mentorId);
        careerPlan.setMentorName(mentorName);
        careerPlan.setUpdatedAt(LocalDateTime.now());
        CareerPlan updated = careerPlanRepository.save(careerPlan);
        return mapToDTO(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer calculateProgressPercentage(Long planId) {
        CareerPlan careerPlan = careerPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Career plan not found: " + planId));
        
        Long totalCriteria = promotionCriteriaRepository.countAllByCareerPlanId(planId);
        if (totalCriteria == 0) return 0;
        
        Long metCriteria = promotionCriteriaRepository.countMetCriteriaByCareerPlanId(planId);
        return (int) Math.round(((double) metCriteria / totalCriteria) * 100);
    }

    @Override
    public void deleteCareerPlan(Long planId) {
        CareerPlan careerPlan = careerPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Career plan not found: " + planId));
        careerPlanRepository.delete(careerPlan);
    }

    private void recalculateProgress(CareerPlan careerPlan) {
        Long totalCriteria = promotionCriteriaRepository.countAllByCareerPlanId(careerPlan.getId());
        if (totalCriteria == 0) {
            careerPlan.setProgressPercentage(0);
            return;
        }
        
        Long metCriteria = promotionCriteriaRepository.countMetCriteriaByCareerPlanId(careerPlan.getId());
        int progress = (int) Math.round(((double) metCriteria / totalCriteria) * 100);
        careerPlan.setProgressPercentage(progress);
    }

    private CareerPlanDTO mapToDTO(CareerPlan careerPlan) {
        CareerPlanDTO dto = modelMapper.map(careerPlan, CareerPlanDTO.class);
        
        List<PromotionCriteria> criteria = promotionCriteriaRepository.findByCareerPlanId(careerPlan.getId());
        dto.setPromotionCriteria(criteria.stream()
                .map(c -> modelMapper.map(c, PromotionCriteriaDTO.class))
                .collect(Collectors.toList()));
        
        return dto;
    }
}
