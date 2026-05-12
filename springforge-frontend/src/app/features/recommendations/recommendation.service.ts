import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Recommendation, CompatibilityScore } from './recommendation.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class RecommendationService {
  private baseUrl = `${environment.apiUrl}/api/v1/recommendations`;

  constructor(private http: HttpClient) {}

  getRecommendations(config: any): Observable<Recommendation[]> {
    return this.http.post<Recommendation[]>(this.baseUrl, config);
  }

  getCompatibilityScore(config: any): Observable<CompatibilityScore> {
    return this.http.post<CompatibilityScore>(`${this.baseUrl}/score`, config);
  }
}
