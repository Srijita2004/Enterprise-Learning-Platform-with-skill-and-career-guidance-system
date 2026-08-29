package com.skillspherenexus.careerservice.repository;

import com.skillspherenexus.careerservice.entity.PromotionCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionCriteriaRepository extends JpaRepository<PromotionCriteria, Long> {
    
    List<PromotionCriteria> findByCareerPlanId(Long careerPlanId);
    
    @Query("SELECT COUNT(pc) FROM PromotionCriteria pc WHERE pc.careerPlan.id = :careerPlanId AND pc.isMet = true")
    Long countMetCriteriaByCareerPlanId(@Param("careerPlanId") Long careerPlanId);
    
    @Query("SELECT COUNT(pc) FROM PromotionCriteria pc WHERE pc.careerPlan.id = :careerPlanId")
    Long countAllByCareerPlanId(@Param("careerPlanId") Long careerPlanId);
    
    @Query("SELECT pc FROM PromotionCriteria pc WHERE pc.careerPlan.id = :careerPlanId AND pc.isMet = false ORDER BY pc.sequenceOrder")
    List<PromotionCriteria> findPendingCriteria(@Param("careerPlanId") Long careerPlanId);
}
