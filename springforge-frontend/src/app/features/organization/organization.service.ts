import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Organization, OrganizationMember, ApiKey, UsageReport } from './organization.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class OrganizationService {
  private baseUrl = `${environment.apiUrl}/api/v1/organizations`;

  constructor(private http: HttpClient) {}

  create(org: Partial<Organization>): Observable<Organization> {
    return this.http.post<Organization>(this.baseUrl, org);
  }

  getById(id: string): Observable<Organization> {
    return this.http.get<Organization>(`${this.baseUrl}/${id}`);
  }

  update(id: string, org: Partial<Organization>): Observable<Organization> {
    return this.http.put<Organization>(`${this.baseUrl}/${id}`, org);
  }

  getMembers(orgId: string): Observable<OrganizationMember[]> {
    return this.http.get<OrganizationMember[]>(`${this.baseUrl}/${orgId}/members`);
  }

  addMember(orgId: string, member: Partial<OrganizationMember>): Observable<OrganizationMember> {
    return this.http.post<OrganizationMember>(`${this.baseUrl}/${orgId}/members`, member);
  }

  removeMember(orgId: string, memberId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${orgId}/members/${memberId}`);
  }

  changeMemberRole(orgId: string, memberId: string, role: string): Observable<OrganizationMember> {
    return this.http.put<OrganizationMember>(`${this.baseUrl}/${orgId}/members/${memberId}/role`, { role });
  }

  generateApiKey(orgId: string, name: string, scopes: string[]): Observable<{ key: string }> {
    return this.http.post<{ key: string }>(`${this.baseUrl}/${orgId}/api-keys`, { name, scopes });
  }

  listApiKeys(orgId: string): Observable<ApiKey[]> {
    return this.http.get<ApiKey[]>(`${this.baseUrl}/${orgId}/api-keys`);
  }

  revokeApiKey(orgId: string, keyId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${orgId}/api-keys/${keyId}`);
  }

  getUsage(orgId: string): Observable<UsageReport> {
    return this.http.get<UsageReport>(`${this.baseUrl}/${orgId}/usage`);
  }

  upgrade(orgId: string, plan: string): Observable<Organization> {
    return this.http.post<Organization>(`${this.baseUrl}/${orgId}/upgrade`, { plan });
  }
}
