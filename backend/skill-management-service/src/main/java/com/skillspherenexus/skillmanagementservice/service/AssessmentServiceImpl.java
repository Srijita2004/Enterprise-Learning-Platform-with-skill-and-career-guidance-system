package com.skillspherenexus.skillmanagementservice.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.skillspherenexus.skillmanagementservice.dto.AssessmentRequestDTO;
import com.skillspherenexus.skillmanagementservice.dto.AssessmentResponseDTO;
import com.skillspherenexus.skillmanagementservice.entity.Assessment;
import com.skillspherenexus.skillmanagementservice.repository.AssessmentRepository;

@Service
public class AssessmentServiceImpl implements AssessmentService {

    private static final Logger logger = LoggerFactory.getLogger(AssessmentServiceImpl.class);

    @Autowired
    private AssessmentRepository repository;

    @Override
    public AssessmentResponseDTO saveAssessment(AssessmentRequestDTO request) {

        Assessment assessment = new Assessment();

        assessment.setEmployeeId(request.getEmployeeId());
        assessment.setSkillId(request.getSkillId());
        assessment.setScore(request.getScore());
        assessment.setVerified(request.getVerified());

        Assessment saved = repository.save(assessment);

        return convertToResponse(saved);
    }

    @Override
    public List<AssessmentResponseDTO> getAllAssessments() {

        return repository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AssessmentResponseDTO getAssessmentById(Long id) {

        Assessment assessment = repository.findById(id).orElse(null);

        if (assessment == null) {
            logger.warn("Assessment not found: id={}", id);
            return null;
        }

        return convertToResponse(assessment);
    }

    @Override
    public AssessmentResponseDTO updateAssessment(Long id,
                                                  AssessmentRequestDTO request) {

        Assessment existing = repository.findById(id).orElse(null);

        if (existing == null) {
            logger.warn("Attempted update on nonexistent assessment: id={}", id);
            return null;
        }

        existing.setEmployeeId(request.getEmployeeId());
        existing.setSkillId(request.getSkillId());
        existing.setScore(request.getScore());
        existing.setVerified(request.getVerified());

        Assessment updated = repository.save(existing);

        return convertToResponse(updated);
    }

    @Override
    public void deleteAssessment(Long id) {
        repository.deleteById(id);
    }

    private AssessmentResponseDTO convertToResponse(Assessment assessment) {

        AssessmentResponseDTO response = new AssessmentResponseDTO();

        response.setAssessmentId(assessment.getAssessmentId());
        response.setEmployeeId(assessment.getEmployeeId());
        response.setSkillId(assessment.getSkillId());
        response.setScore(assessment.getScore());
        response.setVerified(assessment.getVerified());

        return response;
    }
}