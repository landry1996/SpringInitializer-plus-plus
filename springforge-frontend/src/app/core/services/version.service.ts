import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export interface SpringBootVersion {
  id: string;
  name: string;
  status: 'GA' | 'RC' | 'SNAPSHOT';
}

@Injectable({ providedIn: 'root' })
export class VersionService {
  private readonly SPRING_INITIALIZR_URL = 'https://start.spring.io/metadata/client';

  private readonly fallbackVersions: SpringBootVersion[] = [
    { id: '3.4.1', name: '3.4.1', status: 'GA' },
    { id: '3.3.7', name: '3.3.7', status: 'GA' },
    { id: '3.2.12', name: '3.2.12', status: 'GA' },
    { id: '3.1.12', name: '3.1.12', status: 'GA' },
    { id: '3.0.13', name: '3.0.13', status: 'GA' },
    { id: '2.7.18', name: '2.7.18 (Legacy)', status: 'GA' }
  ];

  readonly springBootVersions = signal<SpringBootVersion[]>(this.fallbackVersions);
  readonly loading = signal(false);

  readonly javaVersions = [
    { id: '11', label: 'Java 11 (LTS)' },
    { id: '12', label: 'Java 12' },
    { id: '13', label: 'Java 13' },
    { id: '14', label: 'Java 14' },
    { id: '15', label: 'Java 15' },
    { id: '16', label: 'Java 16' },
    { id: '17', label: 'Java 17 (LTS)' },
    { id: '18', label: 'Java 18' },
    { id: '19', label: 'Java 19' },
    { id: '20', label: 'Java 20' },
    { id: '21', label: 'Java 21 (LTS - Recommended)' },
    { id: '22', label: 'Java 22' },
    { id: '23', label: 'Java 23' },
    { id: '24', label: 'Java 24' },
    { id: '25', label: 'Java 25 (LTS)' }
  ];

  constructor(private http: HttpClient) {
    this.fetchVersions();
  }

  fetchVersions(): void {
    this.loading.set(true);
    this.http.get<any>(this.SPRING_INITIALIZR_URL).subscribe({
      next: (data) => {
        if (data?.bootVersion?.values) {
          const versions: SpringBootVersion[] = data.bootVersion.values
            .filter((v: any) => !v.id.includes('SNAPSHOT') && !v.id.includes('-M') && !v.id.includes('-RC'))
            .map((v: any) => ({ id: v.id, name: v.name || v.id, status: 'GA' as const }));
          if (versions.length > 0) {
            this.springBootVersions.set(versions);
          }
        }
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }

  getMinJavaVersion(springBootVersion: string): number {
    if (springBootVersion.startsWith('4.')) return 21;
    if (springBootVersion.startsWith('3.')) return 17;
    return 11;
  }

  isCompatible(javaVersion: string, springBootVersion: string): boolean {
    const java = parseInt(javaVersion);
    const minJava = this.getMinJavaVersion(springBootVersion);
    return java >= minJava;
  }

  getCompatibilityMessage(javaVersion: string, springBootVersion: string): string | null {
    if (this.isCompatible(javaVersion, springBootVersion)) return null;
    const minJava = this.getMinJavaVersion(springBootVersion);
    return `Spring Boot ${springBootVersion} requires Java ${minJava}+. Please select Java ${minJava} or higher.`;
  }
}
