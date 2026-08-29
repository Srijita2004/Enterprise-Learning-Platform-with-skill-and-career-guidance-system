import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

import {
  LearnerDashboard
} from '../models/learner-dashboard.model';

import { HrDashboard } from '../models/hr-dashboard.model';

@Injectable({
  providedIn: 'root'
})
export class LearnerDashboardService {

  private readonly http =
    inject(HttpClient);

  private readonly apiUrl =
    environment.learningApiUrl;

  getHrDashboard(): Observable<HrDashboard> {
    return this.http.get<HrDashboard>(
      `${this.apiUrl}/dashboard/hr`
    );
  }

  getLearnerDashboard(
    learnerId: string
  ): Observable<LearnerDashboard> {
    return this.http.get<LearnerDashboard>(
      `${this.apiUrl}/dashboard/learners/${learnerId}`
    );
  }
}