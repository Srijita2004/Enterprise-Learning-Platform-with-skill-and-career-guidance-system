package com.skillspherenexus.careerservice.repository;

import com.skillspherenexus.careerservice.entity.SkillGap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillGapRepository extends JpaRepository<SkillGap, Long> {
    
    List<SkillGap> findByCareerPlanId(Long careerPlanId);
    
    @Query("SELECT sg FROM SkillGap sg WHERE sg.careerPlan.id = :careerPlanId AND sg.filledDate IS NULL ORDER BY sg.gapLevel DESC")
    List<SkillGap> findUnfilledGapsByCareerPlan(@Param("careerPlanId") Long careerPlanId);
    
    @Query("SELECT sg FROM SkillGap sg WHERE sg.careerPlan.id = :careerPlanId AND sg.filledDate IS NOT NULL")
    List<SkillGap> findFilledGapsByCareerPlan(@Param("careerPlanId") Long careerPlanId);
    
    @Query("SELECT COUNT(sg) FROM SkillGap sg WHERE sg.careerPlan.id = :careerPlanId")
    Long countGapsByCareerPlan(@Param("careerPlanId") Long careerPlanId);
}
