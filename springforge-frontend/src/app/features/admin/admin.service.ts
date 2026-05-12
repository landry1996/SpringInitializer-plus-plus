import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AdminUser, AuditLog, DashboardStats } from './admin.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private baseUrl = `${environment.apiUrl}/api/v1/admin`;

  constructor(private http: HttpClient) {}

  getDashboard(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${this.baseUrl}/dashboard`);
  }

  getUsers(page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get(`${this.baseUrl}/users`, { params });
  }

  createUser(user: Partial<AdminUser>): Observable<AdminUser> {
    return this.http.post<AdminUser>(`${this.baseUrl}/users`, user);
  }

  updateUser(id: string, user: Partial<AdminUser>): Observable<AdminUser> {
    return this.http.put<AdminUser>(`${this.baseUrl}/users/${id}`, user);
  }

  changeRole(id: string, role: string): Observable<AdminUser> {
    return this.http.put<AdminUser>(`${this.baseUrl}/users/${id}/role`, { role });
  }

  deactivateUser(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/users/${id}`);
  }

  getAuditLogs(page = 0, size = 50, action?: string, userId?: string): Observable<any> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (action) params = params.set('action', action);
    if (userId) params = params.set('userId', userId);
    return this.http.get(`${this.baseUrl}/audit`, { params });
  }

  getPendingBlueprints(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/blueprints/pending`);
  }

  approveBlueprint(id: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/blueprints/${id}/approve`, {});
  }

  rejectBlueprint(id: string): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/blueprints/${id}/reject`, {});
  }
}
