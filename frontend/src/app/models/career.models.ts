export interface PromotionCriteria {
  criteriaId: number;
  name: string;
  description: string;
  isMet: boolean;
  type: 'SKILL' | 'CERTIFICATION' | 'TENURE' | 'ASSESSMENT';
}

export interface CareerPlan {
  planId: number;
  employeeId: number;
  employeeName: string;
  currentRole: string;
  targetRole: string;
  progress: number;
  mentorName: string;
  skillsRequired: string[];
  skillsAcquired: string[];
  skillGaps: string[];
  jobMatchesCount: number;
  promotionCriteria: PromotionCriteria[];
}

export interface JobOpportunity {
  jobId: number;
  title: string;
  department: string;
  location: string;
  matchScore: number;
  skillsRequired: string[];
  description: string;
  salary: number;
}

export interface DepartmentSkillCoverage {
  department: string;
  coverage: number;
}

export interface TrainingEffectiveness {
  courseName: string;
  enrollments: number;
  completionRate: number;
  scoreImprovement: number;
}

export interface CareerDashboard {
  totalPlans: number;
  promotionsAnnually: number;
  departmentSkillCoverage: number;
  jobMatches: number;
  departmentCoverages: DepartmentSkillCoverage[];
  effectivenessReports: TrainingEffectiveness[];
}

export interface AiSkillGap {
  skillName: string;
  currentLevel: number;
  requiredLevel: number;
  gapLevel: number;
  priority: 'HIGH' | 'MEDIUM' | 'LOW';
  recommendedAction: string;
  targetCourse: string;
}

export interface AiCareerEvaluationResponse {
  employeeId: number;
  employeeName: string;
  currentRole: string;
  targetRole: string;
  matchScore: number;
  cosineSimilarity: number;
  readinessProbability: number;
  readinessTier: 'HIGH_ADVANCEMENT' | 'MODERATE_PROGRESSION' | 'FOUNDATION_BUILDING';
  skillGaps: AiSkillGap[];
  topStrengths: string[];
  recommendedCourses: string[];
  aiExecutiveSummary: string;
  strategicNextSteps: string[];
  aiModelEngine: string;
  generatedAt: string;
}

export interface AiCareerEvaluationRequest {
  employeeId: number;
  employeeName: string;
  currentRole?: string;
  targetRole?: string;
  skills?: { skillId?: number; skillName: string; proficiencyLevel: number; yearsExperience?: number }[];
  assessments?: { skillId?: number; score: number; verified?: boolean }[];
  certifications?: string[];
  yearsExperience?: number;
}
