package com.skillspherenexus.careerservice.service;

import com.skillspherenexus.careerservice.dto.CareerDashboardDTO;

public interface DashboardService {
    
    CareerDashboardDTO getExecutiveDashboard();
    
    Integer calculateDepartmentSkillCoverage(String department);
    
    Long countActiveCareerPlans();
    
    Long countAnnualPromotions();
    
    Long countOpenJobOpportunities();
    
    Double getAverageCareerPlanProgress();
}
