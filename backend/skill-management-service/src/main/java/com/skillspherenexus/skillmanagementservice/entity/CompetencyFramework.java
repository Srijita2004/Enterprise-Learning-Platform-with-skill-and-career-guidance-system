package com.skillspherenexus.skillmanagementservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "competency_frameworks")
public class CompetencyFramework {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer frameworkId;

    @Column(nullable = false)
    private String role;

    @ManyToOne
    @JoinColumn(name = "competency_id", nullable = false)
    private Competency competency;

    @Column(nullable = false)
    private Integer requiredLevel;

    public CompetencyFramework() {
    }

    public CompetencyFramework(Integer frameworkId, String role, Competency competency, Integer requiredLevel) {
        this.frameworkId = frameworkId;
        this.role = role;
        this.competency = competency;
        this.requiredLevel = requiredLevel;
    }

    // Getters and Setters
    public Integer getFrameworkId() { return frameworkId; }
    public void setFrameworkId(Integer frameworkId) { this.frameworkId = frameworkId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Competency getCompetency() { return competency; }
    public void setCompetency(Competency competency) { this.competency = competency; }

    public Integer getRequiredLevel() { return requiredLevel; }
    public void setRequiredLevel(Integer requiredLevel) { this.requiredLevel = requiredLevel; }
}
