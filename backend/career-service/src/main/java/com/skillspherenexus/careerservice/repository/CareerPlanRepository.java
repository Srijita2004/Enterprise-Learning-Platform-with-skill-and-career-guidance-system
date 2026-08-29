package com.skillspherenexus.careerservice.repository;

import com.skillspherenexus.careerservice.entity.CareerPlan;
import com.skillspherenexus.careerservice.entity.CareerPlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CareerPlanRepository extends JpaRepository<CareerPlan, Long> {
    
    Optional<CareerPlan> findByEmployeeId(Long employeeId);
    
    List<CareerPlan> findByStatus(CareerPlanStatus status);
    
    List<CareerPlan> findByMentorId(String mentorId);
    
    @Query("SELECT cp FROM CareerPlan cp WHERE cp.status = :status AND cp.createdAt >= :startDate")
    List<CareerPlan> findActiveCareerPlansSince(@Param("status") CareerPlanStatus status, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(cp) FROM CareerPlan cp WHERE cp.status = :status")
    Long countByStatus(@Param("status") CareerPlanStatus status);
    
    @Query("SELECT cp FROM CareerPlan cp WHERE cp.progressPercentage >= :minProgress AND cp.status = :status")
    List<CareerPlan> findByProgressThreshold(@Param("minProgress") Integer minProgress, @Param("status") CareerPlanStatus status);
}
