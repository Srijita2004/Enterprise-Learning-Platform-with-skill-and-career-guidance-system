package com.skillspherenexus.skillmanagementservice.dto;

public class GapResult {

    private final String competencyName;
    private final Integer requiredLevel;
    private final Integer currentLevel;
    private final Integer gap;

    public GapResult(String competencyName, Integer requiredLevel, Integer currentLevel, Integer gap) {
        this.competencyName = competencyName;
        this.requiredLevel = requiredLevel;
        this.currentLevel = currentLevel;
        this.gap = gap;
    }

    public String getCompetencyName() { return competencyName; }
    public Integer getRequiredLevel() { return requiredLevel; }
    public Integer getCurrentLevel() { return currentLevel; }
    public Integer getGap() { return gap; }
}
