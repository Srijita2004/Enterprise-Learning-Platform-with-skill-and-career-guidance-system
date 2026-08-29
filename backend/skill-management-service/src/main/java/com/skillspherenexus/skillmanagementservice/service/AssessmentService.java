package com.skillspherenexus.skillmanagementservice.service;

import java.util.List;

import com.skillspherenexus.skillmanagementservice.dto.AssessmentRequestDTO;
import com.skillspherenexus.skillmanagementservice.dto.AssessmentResponseDTO;

public interface AssessmentService {

    AssessmentResponseDTO saveAssessment(AssessmentRequestDTO request);

    List<AssessmentResponseDTO> getAllAssessments();

    AssessmentResponseDTO getAssessmentById(Long id);

    AssessmentResponseDTO updateAssessment(Long id,
                                           AssessmentRequestDTO request);

    void deleteAssessment(Long id);
}