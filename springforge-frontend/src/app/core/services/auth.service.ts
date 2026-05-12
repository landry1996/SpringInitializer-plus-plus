import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly apiUrl = environment.apiUrl + '/auth';
  private readonly _user = signal<User | null>(null);
  private readonly _token = signal<string | null>(null);

  readonly user = this._user.asReadonly();
  readonly isAuthenticated = computed(() => !!this._token());
  readonly isAdmin = computed(() => this._user()?.role === 'ADMIN');

  constructor(private http: HttpClient, private router: Router) {
    this.loadFromStorage();
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(this.apiUrl + '/login', request).pipe(
      tap(res => this.setSession(res))
    );
  }

  register(request: RegisterRequest): Observable<any> {
    return this.http.post(this.apiUrl + '/register', request);
  }

  refresh(): Observable<AuthResponse> {
    const refreshToken = localStorage.getItem('refreshToken');
    return this.http.post<AuthResponse>(this.apiUrl + '/refresh', { refreshToken }).pipe(
      tap(res => this.setSession(res))
    );
  }

  logout(): void {
    const token = this._token();
    if (token) {
      this.http.post(this.apiUrl + '/logout', {}).subscribe();
    }
    this.clearSession();
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return this._token();
  }

  private setSession(res: AuthResponse): void {
    localStorage.setItem('accessToken', res.accessToken);
    localStorage.setItem('refreshToken', res.refreshToken);
    localStorage.setItem('user', JSON.stringify(res.user));
    this._token.set(res.accessToken);
    this._user.set(res.user);
  }

  private clearSession(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    this._token.set(null);
    this._user.set(null);
  }

  private loadFromStorage(): void {
    const token = localStorage.getItem('accessToken');
    const user = localStorage.getItem('user');
    if (token && user) {
      this._token.set(token);
      this._user.set(JSON.parse(user));
    }
  }
}
