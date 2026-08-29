package com.skillspherenexus.careerservice.service.impl;

import com.skillspherenexus.careerservice.dto.TrainingRecordDTO;
import com.skillspherenexus.careerservice.entity.TrainingRecord;
import com.skillspherenexus.careerservice.entity.TrainingStatus;
import com.skillspherenexus.careerservice.repository.TrainingRecordRepository;
import com.skillspherenexus.careerservice.service.TrainingAnalyticsService;
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
public class TrainingAnalyticsServiceImpl implements TrainingAnalyticsService {

    private final TrainingRecordRepository trainingRecordRepository;
    private final ModelMapper modelMapper;

    @Override
    public TrainingRecordDTO createTrainingRecord(TrainingRecordDTO trainingRecordDTO) {
        TrainingRecord record = modelMapper.map(trainingRecordDTO, TrainingRecord.class);
        record.setStatus(TrainingStatus.ENROLLED);
        record.setEnrollmentDate(LocalDateTime.now());
        TrainingRecord saved = trainingRecordRepository.save(record);
        return modelMapper.map(saved, TrainingRecordDTO.class);
    }

    @Override
    public TrainingRecordDTO updateTrainingRecord(Long recordId, TrainingRecordDTO trainingRecordDTO) {
        TrainingRecord existing = trainingRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Training record not found: " + recordId));
        
        modelMapper.map(trainingRecordDTO, existing);
        existing.setUpdatedAt(LocalDateTime.now());
        TrainingRecord updated = trainingRecordRepository.save(existing);
        return modelMapper.map(updated, TrainingRecordDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public TrainingRecordDTO getTrainingRecord(Long recordId) {
        TrainingRecord record = trainingRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Training record not found: " + recordId));
        return modelMapper.map(record, TrainingRecordDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainingRecordDTO> getTrainingRecordsByEmployee(Long employeeId) {
        return trainingRecordRepository.findByEmployeeId(employeeId)
                .stream()
                .map(tr -> modelMapper.map(tr, TrainingRecordDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainingRecordDTO> getTrainingRecordsByCourse(String courseId) {
        return trainingRecordRepository.findByStatus(TrainingStatus.COMPLETED)
                .stream()
                .filter(tr -> tr.getCourseId().equals(courseId))
                .map(tr -> modelMapper.map(tr, TrainingRecordDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageEmployeeScore(Long employeeId) {
        Double avg = trainingRecordRepository.getAverageScoreByEmployee(employeeId, TrainingStatus.COMPLETED);
        return avg != null ? avg : 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageCourseCompletionRate(String courseId) {
        Long totalEnrolled = trainingRecordRepository.countByCourseIdAndStatus(courseId, TrainingStatus.ENROLLED);
        Long totalCompleted = trainingRecordRepository.countByCourseIdAndStatus(courseId, TrainingStatus.COMPLETED);
        
        if (totalEnrolled == 0) return 0.0;
        return ((double) totalCompleted / totalEnrolled) * 100;
    }

    @Override
    @Transactional(readOnly = true)
    public Double calculateSkillImprovementScore(Long employeeId, String skillName) {
        List<TrainingRecord> records = trainingRecordRepository.findByEmployeeId(employeeId);
        double totalImprovement = records.stream()
                .filter(tr -> tr.getSkillImprovement() != null)
                .mapToDouble(TrainingRecord::getSkillImprovement)
                .sum();
        return totalImprovement > 0 ? totalImprovement : 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getCompletionRatePercentage(String courseId) {
        Double rate = getAverageCourseCompletionRate(courseId);
        return rate.intValue();
    }

    @Override
    public void deleteTrainingRecord(Long recordId) {
        TrainingRecord record = trainingRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Training record not found: " + recordId));
        trainingRecordRepository.delete(record);
    }
}
