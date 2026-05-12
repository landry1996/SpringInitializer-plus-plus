import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Blueprint {
  id: string;
  name: string;
  description: string;
  type: string;
  constraints: string;
  defaults: string;
  structure: string;
  version: number;
  builtIn: boolean;
}

export interface GenerationResponse {
  generationId: string;
  status: string;
  statusUrl: string;
}

export interface GenerationStatus {
  id: string;
  status: string;
  startedAt: string | null;
  completedAt: string | null;
  downloadUrl: string | null;
  errorMessage: string | null;
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getBlueprints(): Observable<Blueprint[]> {
    return this.http.get<Blueprint[]>(this.apiUrl + '/blueprints');
  }

  generateProject(configuration: any): Observable<GenerationResponse> {
    return this.http.post<GenerationResponse>(this.apiUrl + '/projects/generate', { configuration });
  }

  getGenerationStatus(id: string): Observable<GenerationStatus> {
    return this.http.get<GenerationStatus>(this.apiUrl + '/generations/' + id + '/status');
  }

  downloadGeneration(id: string): Observable<Blob> {
    return this.http.get(this.apiUrl + '/generations/' + id + '/download', { responseType: 'blob' });
  }

  checkDependencies(dependencies: string[]): Observable<DependencyCheckResult> {
    return this.http.post<DependencyCheckResult>(this.apiUrl + '/dependencies/check', dependencies);
  }

  validateConfiguration(configuration: any): Observable<ValidationResult> {
    return this.http.post<ValidationResult>(this.apiUrl + '/projects/validate', { configuration });
  }
}

export interface DependencyCheckResult {
  valid: boolean;
  conflicts: string[];
  suggestions: string[];
}

export interface ValidationResult {
  valid: boolean;
  errors: string[];
}
