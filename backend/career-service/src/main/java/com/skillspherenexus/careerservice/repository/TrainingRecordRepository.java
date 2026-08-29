package com.skillspherenexus.careerservice.repository;

import com.skillspherenexus.careerservice.entity.TrainingRecord;
import com.skillspherenexus.careerservice.entity.TrainingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TrainingRecordRepository extends JpaRepository<TrainingRecord, Long> {
    
    List<TrainingRecord> findByEmployeeId(Long employeeId);
    
    List<TrainingRecord> findByStatus(TrainingStatus status);
    
    @Query("SELECT tr FROM TrainingRecord tr WHERE tr.employeeId = :employeeId AND tr.status = :status")
    List<TrainingRecord> findByEmployeeIdAndStatus(@Param("employeeId") Long employeeId, @Param("status") TrainingStatus status);
    
    @Query("SELECT AVG(tr.score) FROM TrainingRecord tr WHERE tr.employeeId = :employeeId AND tr.status = :status")
    Double getAverageScoreByEmployee(@Param("employeeId") Long employeeId, @Param("status") TrainingStatus status);
    
    @Query("SELECT COUNT(tr) FROM TrainingRecord tr WHERE tr.courseId = :courseId AND tr.status = :status")
    Long countByCourseIdAndStatus(@Param("courseId") String courseId, @Param("status") TrainingStatus status);
    
    @Query("SELECT tr FROM TrainingRecord tr WHERE tr.completionDate IS NULL AND tr.enrollmentDate < :cutoffDate AND tr.status = :status")
    List<TrainingRecord> findStaleRecords(@Param("cutoffDate") LocalDateTime cutoffDate, @Param("status") TrainingStatus status);
}
