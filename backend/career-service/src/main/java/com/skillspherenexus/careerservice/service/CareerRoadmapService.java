package com.skillspherenexus.careerservice.service;

import com.skillspherenexus.careerservice.dto.CareerRoadmapDTO;
import java.util.List;
import java.util.Optional;

public interface CareerRoadmapService {

    CareerRoadmapDTO createRoadmap(CareerRoadmapDTO dto);

    CareerRoadmapDTO getRoadmap(Long id);

    List<CareerRoadmapDTO> getAllActiveRoadmaps();

    Optional<CareerRoadmapDTO> findRoadmapByRoles(String sourceRole, String targetRole);

    List<CareerRoadmapDTO> findRoadmapsBySourceRole(String sourceRole);

    CareerRoadmapDTO updateRoadmap(Long id, CareerRoadmapDTO dto);

    void deleteRoadmap(Long id);
}