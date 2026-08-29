export type CompetencyCategory = 'TECHNICAL' | 'DOMAIN' | 'SOFT';

export interface Competency {
  competencyId: number;
  name: string;
  category: CompetencyCategory;
  description: string;
  maxLevel: number;
}

export interface CompetencyFramework {
  frameworkId?: number;
  role: string;
  competency: Competency;
  requiredLevel: number;
}

export interface EmployeeCompetency {
  employeeCompetencyId?: number;
  employeeId: number;
  competency: Competency;
  currentLevel: number;
}

export interface GapResult {
  competencyName: string;
  requiredLevel: number;
  currentLevel: number;
  gap: number;
}

export interface CompetencyRequestDTO {
  name: string;
  category: CompetencyCategory;
  description: string;
  maxLevel: number;
}

export interface CompetencyResponseDTO {
  competencyId: number;
  name: string;
  category: CompetencyCategory;
  description: string;
  maxLevel: number;
}

export interface CompetencyFrameworkRequestDTO {
  role: string;
  competencyId: number;
  requiredLevel: number;
}

export interface CompetencyFrameworkResponseDTO {
  frameworkId: number;
  role: string;
  competencyId: number;
  requiredLevel: number;
}

export interface EmployeeCompetencyRequestDTO {
  employeeId: number;
  competencyId: number;
  currentLevel: number;
}

export interface EmployeeCompetencyResponseDTO {
  employeeCompetencyId: number;
  employeeId: number;
  competencyId: number;
  currentLevel: number;
}