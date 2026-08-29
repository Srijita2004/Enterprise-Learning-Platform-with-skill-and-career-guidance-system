package com.skillspherenexus.careerservice.service;

import com.skillspherenexus.careerservice.dto.TrainingRecordDTO;
import java.util.List;

public interface TrainingAnalyticsService {
    
    TrainingRecordDTO createTrainingRecord(TrainingRecordDTO trainingRecordDTO);
    
    TrainingRecordDTO updateTrainingRecord(Long recordId, TrainingRecordDTO trainingRecordDTO);
    
    TrainingRecordDTO getTrainingRecord(Long recordId);
    
    List<TrainingRecordDTO> getTrainingRecordsByEmployee(Long employeeId);
    
    List<TrainingRecordDTO> getTrainingRecordsByCourse(String courseId);
    
    Double getAverageEmployeeScore(Long employeeId);
    
    Double getAverageCourseCompletionRate(String courseId);
    
    Double calculateSkillImprovementScore(Long employeeId, String skillName);
    
    Integer getCompletionRatePercentage(String courseId);
    
    void deleteTrainingRecord(Long recordId);
}
