import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Blueprint, BlueprintSearchCriteria, PageResponse } from './blueprint.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class MarketplaceService {
  private baseUrl = `${environment.apiUrl}/api/v1/marketplace/blueprints`;

  constructor(private http: HttpClient) {}

  search(criteria: BlueprintSearchCriteria, page = 0, size = 20): Observable<PageResponse<Blueprint>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (criteria.query) params = params.set('query', criteria.query);
    if (criteria.category) params = params.set('category', criteria.category);
    if (criteria.author) params = params.set('author', criteria.author);
    if (criteria.minRating) params = params.set('minRating', criteria.minRating);
    if (criteria.sortBy) params = params.set('sortBy', criteria.sortBy);
    if (criteria.sortDir) params = params.set('sortDir', criteria.sortDir);
    return this.http.get<PageResponse<Blueprint>>(this.baseUrl, { params });
  }

  getById(id: string): Observable<Blueprint> {
    return this.http.get<Blueprint>(`${this.baseUrl}/${id}`);
  }

  create(blueprint: Partial<Blueprint>): Observable<Blueprint> {
    return this.http.post<Blueprint>(this.baseUrl, blueprint);
  }

  update(id: string, blueprint: Partial<Blueprint>): Observable<Blueprint> {
    return this.http.put<Blueprint>(`${this.baseUrl}/${id}`, blueprint);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  publish(id: string): Observable<Blueprint> {
    return this.http.post<Blueprint>(`${this.baseUrl}/${id}/publish`, {});
  }

  rate(id: string, rating: number, comment?: string): Observable<Blueprint> {
    return this.http.post<Blueprint>(`${this.baseUrl}/${id}/rate`, { rating, comment });
  }

  download(id: string): Observable<Blueprint> {
    return this.http.get<Blueprint>(`${this.baseUrl}/${id}/download`);
  }

  getPopular(): Observable<Blueprint[]> {
    return this.http.get<Blueprint[]>(`${this.baseUrl}/popular`);
  }

  getTopRated(): Observable<Blueprint[]> {
    return this.http.get<Blueprint[]>(`${this.baseUrl}/top-rated`);
  }
}
