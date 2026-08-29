package com.skillspherenexus.careerservice.repository;

import com.skillspherenexus.careerservice.entity.CareerRoadmap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CareerRoadmapRepository extends JpaRepository<CareerRoadmap, Long> {
    
    @Query("SELECT cr FROM CareerRoadmap cr WHERE cr.sourceRole = :sourceRole AND cr.targetRole = :targetRole AND cr.isActive = true")
    Optional<CareerRoadmap> findByRoles(@Param("sourceRole") String sourceRole, @Param("targetRole") String targetRole);
    
    @Query("SELECT cr FROM CareerRoadmap cr WHERE cr.sourceRole = :sourceRole AND cr.isActive = true")
    List<CareerRoadmap> findBySourceRole(@Param("sourceRole") String sourceRole);
    
    @Query("SELECT cr FROM CareerRoadmap cr WHERE cr.isActive = true ORDER BY cr.sourceRole, cr.targetRole")
    List<CareerRoadmap> findAllActive();
}
