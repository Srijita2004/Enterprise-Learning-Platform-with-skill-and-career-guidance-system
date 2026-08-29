package com.skillspherenexus.skillmanagementservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.security.access.prepost.PreAuthorize;
import com.skillspherenexus.skillmanagementservice.dto.SkillRequestDTO;
import com.skillspherenexus.skillmanagementservice.dto.SkillResponseDTO;
import com.skillspherenexus.skillmanagementservice.service.SkillService;

@RestController
@RequestMapping("/api/skills")
public class SkillController {

    @Autowired
    private SkillService skillService;

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PostMapping
    public SkillResponseDTO saveSkill(@RequestBody SkillRequestDTO request) {
        return skillService.saveSkill(request);
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR','EMPLOYEE','LEARNER')")
    @GetMapping
    public List<SkillResponseDTO> getAllSkills() {
        return skillService.getAllSkills();
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR','EMPLOYEE','LEARNER')")
    @GetMapping("/{id}")
    public SkillResponseDTO getSkillById(@PathVariable Integer id) {
        return skillService.getSkillById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @PutMapping("/{id}")
    public SkillResponseDTO updateSkill(@PathVariable Integer id,
                                        @RequestBody SkillRequestDTO request) {
        return skillService.updateSkill(id, request);
    }

    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    @DeleteMapping("/{id}")
    public String deleteSkill(@PathVariable Integer id) {
        skillService.deleteSkill(id);
        return "Skill deleted successfully";
    }
}