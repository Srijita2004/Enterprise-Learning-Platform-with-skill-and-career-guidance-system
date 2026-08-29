package com.skillspherenexus.careerservice.service.impl;

import com.skillspherenexus.careerservice.dto.CareerRoadmapDTO;
import com.skillspherenexus.careerservice.entity.CareerRoadmap;
import com.skillspherenexus.careerservice.repository.CareerRoadmapRepository;
import com.skillspherenexus.careerservice.service.CareerRoadmapService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CareerRoadmapServiceImpl implements CareerRoadmapService {

    private final CareerRoadmapRepository roadmapRepository;
    private final ModelMapper modelMapper;

    @Override
    public CareerRoadmapDTO createRoadmap(CareerRoadmapDTO dto) {
        CareerRoadmap entity = modelMapper.map(dto, CareerRoadmap.class);
        entity.setId(null);
        CareerRoadmap saved = roadmapRepository.save(entity);
        return modelMapper.map(saved, CareerRoadmapDTO.class);
    }

    @Override
    public CareerRoadmapDTO getRoadmap(Long id) {
        CareerRoadmap entity = roadmapRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Roadmap not found: " + id));
        return modelMapper.map(entity, CareerRoadmapDTO.class);
    }

    @Override
    public List<CareerRoadmapDTO> getAllActiveRoadmaps() {
        return roadmapRepository.findAllActive().stream()
                .map(r -> modelMapper.map(r, CareerRoadmapDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CareerRoadmapDTO> findRoadmapByRoles(String sourceRole, String targetRole) {
        return roadmapRepository.findByRoles(sourceRole, targetRole)
                .map(r -> modelMapper.map(r, CareerRoadmapDTO.class));
    }

    @Override
    public List<CareerRoadmapDTO> findRoadmapsBySourceRole(String sourceRole) {
        return roadmapRepository.findBySourceRole(sourceRole).stream()
                .map(r -> modelMapper.map(r, CareerRoadmapDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public CareerRoadmapDTO updateRoadmap(Long id, CareerRoadmapDTO dto) {
        CareerRoadmap existing = roadmapRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Roadmap not found: " + id));
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setSourceRole(dto.getSourceRole());
        existing.setTargetRole(dto.getTargetRole());
        existing.setEstimatedDurationMonths(dto.getEstimatedDurationMonths());
        existing.setRequiredSkills(dto.getRequiredSkills());
        existing.setSuggestedCourses(dto.getSuggestedCourses());
        if (dto.getIsActive() != null) {
            existing.setIsActive(dto.getIsActive());
        }
        CareerRoadmap saved = roadmapRepository.save(existing);
        return modelMapper.map(saved, CareerRoadmapDTO.class);
    }

    @Override
    public void deleteRoadmap(Long id) {
        if (!roadmapRepository.existsById(id)) {
            throw new IllegalArgumentException("Roadmap not found: " + id);
        }
        roadmapRepository.deleteById(id);
    }
}