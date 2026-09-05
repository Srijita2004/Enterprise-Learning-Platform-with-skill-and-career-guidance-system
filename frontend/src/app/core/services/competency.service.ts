import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import {
  Competency,
  CompetencyCategory,
  CompetencyFramework,
  EmployeeCompetency,
  GapResult,
  CompetencyRequestDTO,
  CompetencyResponseDTO,
  CompetencyFrameworkRequestDTO,
  CompetencyFrameworkResponseDTO,
  EmployeeCompetencyRequestDTO,
  EmployeeCompetencyResponseDTO
} from '../../learning/models/competency';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CompetencyService {
  private readonly baseUrl = `${environment.apiUrl}/competencies`;
  private readonly competencyCache = new Map<number, Competency>();

  // =========================================================
  // COMPLETE ENTERPRISE COMPETENCY CATALOG (10 PROFICIENCIES)
  // =========================================================
  private mockCompetencies: Competency[] = [
    { competencyId: 1, name: 'Software Architecture', category: 'TECHNICAL', description: 'System modeling, design patterns, microservices choreography, resilience', maxLevel: 5 },
    { competencyId: 2, name: 'Cloud Native & Kubernetes', category: 'TECHNICAL', description: 'Multi-region Kubernetes, Terraform IaC, Docker containerization', maxLevel: 5 },
    { competencyId: 3, name: 'Microservices & Event Streaming', category: 'TECHNICAL', description: 'Kafka event fabrics, reactive messaging, Spring Cloud Gateway', maxLevel: 5 },
    { competencyId: 4, name: 'Problem Solving & Algorithms', category: 'SOFT', description: 'Analytical reasoning, algorithmic optimization, distributed complexity', maxLevel: 5 },
    { competencyId: 5, name: 'Technical Leadership & Mentorship', category: 'SOFT', description: 'Architecture review boards, engineering mentoring, talent development', maxLevel: 5 },
    { competencyId: 6, name: 'Cybersecurity & Governance', category: 'DOMAIN', description: 'Zero-trust security, SOC2/ISO compliance, vulnerability threat modeling', maxLevel: 5 },
    { competencyId: 7, name: 'Automated QA & CI/CD Pipelines', category: 'TECHNICAL', description: 'End-to-end test automation, release engineering, performance benchmarking', maxLevel: 5 },
    { competencyId: 8, name: 'Frontend Systems & UI/UX', category: 'TECHNICAL', description: 'Angular 21 Signals, responsive design systems, sub-second LCP optimization', maxLevel: 5 },
    { competencyId: 9, name: 'Database Scalability & Indexing', category: 'TECHNICAL', description: 'PostgreSQL optimization, distributed partitioning, low-latency clustering', maxLevel: 5 },
    { competencyId: 10, name: 'AI & Vector Space Machine Learning', category: 'TECHNICAL', description: 'MLOps pipelines, RAG vector similarity engines, LLM orchestration', maxLevel: 5 }
  ];

  // =========================================================
  // ROLE FRAMEWORKS (COMPETENCY REQUIREMENTS PER ROLE)
  // =========================================================
  private mockFrameworks: CompetencyFramework[] = [
    // Senior Java Developer
    { frameworkId: 1, role: 'Senior Java Developer', competency: this.mockCompetencies[0], requiredLevel: 4 },
    { frameworkId: 2, role: 'Senior Java Developer', competency: this.mockCompetencies[2], requiredLevel: 4 },
    { frameworkId: 3, role: 'Senior Java Developer', competency: this.mockCompetencies[3], requiredLevel: 4 },
    { frameworkId: 4, role: 'Senior Java Developer', competency: this.mockCompetencies[8], requiredLevel: 3 },
    
    // Principal Architect
    { frameworkId: 5, role: 'Principal Architect', competency: this.mockCompetencies[0], requiredLevel: 5 },
    { frameworkId: 6, role: 'Principal Architect', competency: this.mockCompetencies[1], requiredLevel: 5 },
    { frameworkId: 7, role: 'Principal Architect', competency: this.mockCompetencies[4], requiredLevel: 5 },
    { frameworkId: 8, role: 'Principal Architect', competency: this.mockCompetencies[5], requiredLevel: 4 },

    // DevOps Engineer
    { frameworkId: 9, role: 'DevOps Engineer', competency: this.mockCompetencies[1], requiredLevel: 5 },
    { frameworkId: 10, role: 'DevOps Engineer', competency: this.mockCompetencies[6], requiredLevel: 4 },
    { frameworkId: 11, role: 'DevOps Engineer', competency: this.mockCompetencies[5], requiredLevel: 3 },

    // QA Lead
    { frameworkId: 12, role: 'QA Lead', competency: this.mockCompetencies[6], requiredLevel: 5 },
    { frameworkId: 13, role: 'QA Lead', competency: this.mockCompetencies[3], requiredLevel: 4 },
    { frameworkId: 14, role: 'QA Lead', competency: this.mockCompetencies[4], requiredLevel: 4 },

    // Product Specialist
    { frameworkId: 15, role: 'Product Specialist', competency: this.mockCompetencies[3], requiredLevel: 4 },
    { frameworkId: 16, role: 'Product Specialist', competency: this.mockCompetencies[4], requiredLevel: 4 },
    { frameworkId: 17, role: 'Product Specialist', competency: this.mockCompetencies[5], requiredLevel: 4 }
  ];

  // =========================================================
  // EMPLOYEE COMPETENCY ASSIGNMENTS (ALL 10 EMPLOYEES)
  // =========================================================
  private mockEmployeeCompetencies: EmployeeCompetency[] = [
    // Srijita (ID: 1)
    { employeeCompetencyId: 101, employeeId: 1, competency: this.mockCompetencies[0], currentLevel: 4 },
    { employeeCompetencyId: 102, employeeId: 1, competency: this.mockCompetencies[2], currentLevel: 4 },
    { employeeCompetencyId: 103, employeeId: 1, competency: this.mockCompetencies[3], currentLevel: 4 },
    { employeeCompetencyId: 104, employeeId: 1, competency: this.mockCompetencies[8], currentLevel: 4 },
    { employeeCompetencyId: 105, employeeId: 1, competency: this.mockCompetencies[1], currentLevel: 2 },

    // Alex Vance (ID: 101)
    { employeeCompetencyId: 201, employeeId: 101, competency: this.mockCompetencies[1], currentLevel: 5 },
    { employeeCompetencyId: 202, employeeId: 101, competency: this.mockCompetencies[0], currentLevel: 4 },
    { employeeCompetencyId: 203, employeeId: 101, competency: this.mockCompetencies[6], currentLevel: 4 },

    // Marcus Brodie (ID: 102)
    { employeeCompetencyId: 301, employeeId: 102, competency: this.mockCompetencies[0], currentLevel: 5 },
    { employeeCompetencyId: 302, employeeId: 102, competency: this.mockCompetencies[1], currentLevel: 5 },
    { employeeCompetencyId: 303, employeeId: 102, competency: this.mockCompetencies[4], currentLevel: 5 },
    { employeeCompetencyId: 304, employeeId: 102, competency: this.mockCompetencies[5], currentLevel: 5 },

    // Sarah Jenkins (ID: 103)
    { employeeCompetencyId: 401, employeeId: 103, competency: this.mockCompetencies[6], currentLevel: 5 },
    { employeeCompetencyId: 402, employeeId: 103, competency: this.mockCompetencies[3], currentLevel: 4 },
    { employeeCompetencyId: 403, employeeId: 103, competency: this.mockCompetencies[4], currentLevel: 3 },

    // John Smith (ID: 106)
    { employeeCompetencyId: 501, employeeId: 106, competency: this.mockCompetencies[7], currentLevel: 4 },
    { employeeCompetencyId: 502, employeeId: 106, competency: this.mockCompetencies[3], currentLevel: 4 },
    { employeeCompetencyId: 503, employeeId: 106, competency: this.mockCompetencies[0], currentLevel: 3 },

    // Jane Doe (ID: 107)
    { employeeCompetencyId: 601, employeeId: 107, competency: this.mockCompetencies[9], currentLevel: 5 },
    { employeeCompetencyId: 602, employeeId: 107, competency: this.mockCompetencies[3], currentLevel: 5 },
    { employeeCompetencyId: 603, employeeId: 107, competency: this.mockCompetencies[1], currentLevel: 3 },

    // David Miller (ID: 108)
    { employeeCompetencyId: 701, employeeId: 108, competency: this.mockCompetencies[1], currentLevel: 4 },
    { employeeCompetencyId: 702, employeeId: 108, competency: this.mockCompetencies[6], currentLevel: 4 },
    { employeeCompetencyId: 703, employeeId: 108, competency: this.mockCompetencies[5], currentLevel: 3 },

    // Elena Rostova (ID: 109)
    { employeeCompetencyId: 801, employeeId: 109, competency: this.mockCompetencies[5], currentLevel: 5 },
    { employeeCompetencyId: 802, employeeId: 109, competency: this.mockCompetencies[0], currentLevel: 4 },
    { employeeCompetencyId: 803, employeeId: 109, competency: this.mockCompetencies[4], currentLevel: 4 },

    // Michael Chang (ID: 110)
    { employeeCompetencyId: 901, employeeId: 110, competency: this.mockCompetencies[0], currentLevel: 3 },
    { employeeCompetencyId: 902, employeeId: 110, competency: this.mockCompetencies[7], currentLevel: 4 },
    { employeeCompetencyId: 903, employeeId: 110, competency: this.mockCompetencies[8], currentLevel: 3 },

    // Priya Sharma (ID: 111)
    { employeeCompetencyId: 1001, employeeId: 111, competency: this.mockCompetencies[8], currentLevel: 5 },
    { employeeCompetencyId: 1002, employeeId: 111, competency: this.mockCompetencies[2], currentLevel: 4 },
    { employeeCompetencyId: 1003, employeeId: 111, competency: this.mockCompetencies[0], currentLevel: 3 }
  ];

  constructor(private http: HttpClient) {
    this.refreshCompetencyCache(this.mockCompetencies);
  }

  private refreshCompetencyCache(competencies: Competency[]): void {
    this.competencyCache.clear();
    competencies.forEach(comp => this.competencyCache.set(comp.competencyId, comp));
  }

  private toCompetencyModel(dto: CompetencyResponseDTO): Competency {
    return {
      competencyId: dto.competencyId,
      name: dto.name,
      category: dto.category,
      description: dto.description,
      maxLevel: dto.maxLevel
    };
  }

  private toCompetencyRequest(dto: Competency): CompetencyRequestDTO {
    return {
      name: dto.name,
      category: dto.category,
      description: dto.description,
      maxLevel: dto.maxLevel
    };
  }

  private toFrameworkModel(dto: CompetencyFrameworkResponseDTO): CompetencyFramework {
    const competency = this.competencyCache.get(dto.competencyId) ?? {
      competencyId: dto.competencyId,
      name: 'Software Architecture',
      category: 'TECHNICAL' as CompetencyCategory,
      description: 'System modeling & design',
      maxLevel: 5
    };

    return {
      frameworkId: dto.frameworkId,
      role: dto.role,
      competency,
      requiredLevel: dto.requiredLevel
    };
  }

  private toFrameworkRequest(model: CompetencyFramework): CompetencyFrameworkRequestDTO {
    return {
      role: model.role,
      competencyId: model.competency.competencyId,
      requiredLevel: model.requiredLevel
    };
  }

  private toEmployeeCompetencyModel(dto: EmployeeCompetencyResponseDTO): EmployeeCompetency {
    const competency = this.competencyCache.get(dto.competencyId) ?? {
      competencyId: dto.competencyId,
      name: 'Software Architecture',
      category: 'TECHNICAL' as CompetencyCategory,
      description: 'System modeling',
      maxLevel: 5
    };

    return {
      employeeCompetencyId: dto.employeeCompetencyId,
      employeeId: dto.employeeId,
      competency,
      currentLevel: dto.currentLevel
    };
  }

  private toEmployeeCompetencyRequest(model: EmployeeCompetency): EmployeeCompetencyRequestDTO {
    return {
      employeeId: model.employeeId,
      competencyId: model.competency.competencyId,
      currentLevel: model.currentLevel
    };
  }

  // --- Competency Catalog ---
  getAll(): Observable<Competency[]> {
    return this.http.get<CompetencyResponseDTO[]>(this.baseUrl).pipe(
      map(list => {
        if (!list || list.length === 0) return [...this.mockCompetencies];
        const competencies = list.map(dto => this.toCompetencyModel(dto));
        this.refreshCompetencyCache(competencies);
        return competencies;
      }),
      catchError(() => of([...this.mockCompetencies]))
    );
  }

  getById(id: number): Observable<Competency> {
    return this.http.get<CompetencyResponseDTO>(`${this.baseUrl}/${id}`).pipe(
      map(dto => {
        const competency = this.toCompetencyModel(dto);
        this.competencyCache.set(competency.competencyId, competency);
        return competency;
      }),
      catchError(() => {
        const comp = this.mockCompetencies.find(c => c.competencyId === id) || this.mockCompetencies[0];
        return of({ ...comp });
      })
    );
  }

  create(competency: Competency): Observable<Competency> {
    const newId = this.mockCompetencies.length > 0 ? Math.max(...this.mockCompetencies.map(c => c.competencyId ?? 0)) + 1 : 1;
    const newComp = { ...competency, competencyId: newId };
    this.mockCompetencies.push(newComp);
    this.competencyCache.set(newId, newComp);

    return this.http.post<CompetencyResponseDTO>(this.baseUrl, this.toCompetencyRequest(competency)).pipe(
      map(dto => this.toCompetencyModel(dto)),
      catchError(() => of(newComp))
    );
  }

  update(id: number, competency: Competency): Observable<Competency> {
    const index = this.mockCompetencies.findIndex(c => c.competencyId === id);
    if (index !== -1) {
      this.mockCompetencies[index] = { ...competency, competencyId: id };
      this.competencyCache.set(id, this.mockCompetencies[index]);
    }

    return this.http.put<CompetencyResponseDTO>(`${this.baseUrl}/${id}`, this.toCompetencyRequest(competency)).pipe(
      map(dto => this.toCompetencyModel(dto)),
      catchError(() => of(this.mockCompetencies[index !== -1 ? index : 0]))
    );
  }

  delete(id: number): Observable<void> {
    const index = this.mockCompetencies.findIndex(c => c.competencyId === id);
    if (index !== -1) {
      this.mockCompetencies.splice(index, 1);
      this.competencyCache.delete(id);
    }
    return this.http.delete<void>(`${this.baseUrl}/${id}`).pipe(
      catchError(() => of(undefined))
    );
  }

  // --- Competency Framework ---
  defineFrameworkRequirement(framework: CompetencyFramework): Observable<CompetencyFramework> {
    const newId = this.mockFrameworks.length > 0 ? Math.max(...this.mockFrameworks.map(f => f.frameworkId ?? 0)) + 1 : 1;
    const newFw = { ...framework, frameworkId: newId };
    this.mockFrameworks.push(newFw);

    return this.http.post<CompetencyFrameworkResponseDTO>(`${this.baseUrl}/frameworks`, this.toFrameworkRequest(framework)).pipe(
      map(dto => this.toFrameworkModel(dto)),
      catchError(() => of(newFw))
    );
  }

  getFrameworkForRole(role: string): Observable<CompetencyFramework[]> {
    const fallback = this.mockFrameworks.filter(f => f.role.toLowerCase() === role.toLowerCase());
    return this.http.get<CompetencyFrameworkResponseDTO[]>(`${this.baseUrl}/frameworks/role/${encodeURIComponent(role)}`).pipe(
      map(list => {
        if (!list || list.length === 0) return fallback;
        return list.map(dto => this.toFrameworkModel(dto));
      }),
      catchError(() => of(fallback))
    );
  }

  // --- Employee Competency ---
  recordEmployeeLevel(employeeCompetency: EmployeeCompetency): Observable<EmployeeCompetency> {
    const newId = this.mockEmployeeCompetencies.length + 1;
    const newEc = { ...employeeCompetency, employeeCompetencyId: newId };
    
    const index = this.mockEmployeeCompetencies.findIndex(
      ec => ec.employeeId === employeeCompetency.employeeId && 
            ec.competency.competencyId === employeeCompetency.competency.competencyId
    );
    if (index !== -1) {
      this.mockEmployeeCompetencies[index].currentLevel = employeeCompetency.currentLevel;
    } else {
      this.mockEmployeeCompetencies.push(newEc);
    }

    return this.http.post<EmployeeCompetencyResponseDTO>(`${this.baseUrl}/employee-levels`, this.toEmployeeCompetencyRequest(employeeCompetency)).pipe(
      map(dto => this.toEmployeeCompetencyModel(dto)),
      catchError(() => of(newEc))
    );
  }

  // --- Gap Analysis ---
  analyzeGap(employeeId: string, role: string): Observable<GapResult[]> {
    const requirements = this.mockFrameworks.filter(f => f.role.toLowerCase() === role.toLowerCase());
    const fallbackResults: GapResult[] = requirements.map(req => {
      const empLevel = this.mockEmployeeCompetencies.find(
        ec => ec.employeeId.toString() === employeeId.toString() && 
              ec.competency.competencyId === req.competency.competencyId
      );
      const current = empLevel ? empLevel.currentLevel : 2;
      const gap = Math.max(0, req.requiredLevel - current);
      return {
        competencyName: req.competency.name,
        requiredLevel: req.requiredLevel,
        currentLevel: current,
        gap: gap
      };
    });

    return this.http.get<GapResult[]>(`${this.baseUrl}/gap-analysis`, {
      params: { employeeId, role }
    }).pipe(
      map(res => {
        if (!res || res.length === 0) return fallbackResults;
        return res;
      }),
      catchError(() => of(fallbackResults))
    );
  }
}
