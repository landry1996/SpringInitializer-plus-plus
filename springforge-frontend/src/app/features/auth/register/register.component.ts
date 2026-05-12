import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="auth-container">
      <div class="auth-card">
        <h1>SpringForge</h1>
        <h2>Register</h2>
        @if (error()) {
          <div class="error">{{ error() }}</div>
        }
        <form (ngSubmit)="onSubmit()">
          <div class="form-row">
            <div class="form-group">
              <label>First Name</label>
              <input type="text" [(ngModel)]="firstName" name="firstName" required>
            </div>
            <div class="form-group">
              <label>Last Name</label>
              <input type="text" [(ngModel)]="lastName" name="lastName" required>
            </div>
          </div>
          <div class="form-group">
            <label>Email</label>
            <input type="email" [(ngModel)]="email" name="email" required>
          </div>
          <div class="form-group">
            <label>Password</label>
            <input type="password" [(ngModel)]="password" name="password" required minlength="8">
          </div>
          <button type="submit" [disabled]="loading()">
            {{ loading() ? 'Creating account...' : 'Register' }}
          </button>
        </form>
        <p class="link">Already have an account? <a routerLink="/login">Login</a></p>
      </div>
    </div>
  `,
  styles: [`
    .auth-container { display: flex; justify-content: center; align-items: center; min-height: 100vh; background: #f5f5f5; }
    .auth-card { background: white; padding: 2rem; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); width: 100%; max-width: 450px; }
    h1 { text-align: center; color: #1976d2; margin-bottom: 0.5rem; }
    h2 { text-align: center; color: #333; margin-bottom: 1.5rem; }
    .form-row { display: flex; gap: 1rem; }
    .form-group { margin-bottom: 1rem; flex: 1; }
    label { display: block; margin-bottom: 0.25rem; font-weight: 500; color: #555; }
    input { width: 100%; padding: 0.75rem; border: 1px solid #ddd; border-radius: 4px; font-size: 1rem; box-sizing: border-box; }
    button { width: 100%; padding: 0.75rem; background: #1976d2; color: white; border: none; border-radius: 4px; font-size: 1rem; cursor: pointer; }
    button:hover { background: #1565c0; }
    button:disabled { background: #90caf9; }
    .error { background: #ffebee; color: #c62828; padding: 0.75rem; border-radius: 4px; margin-bottom: 1rem; }
    .link { text-align: center; margin-top: 1rem; }
    .link a { color: #1976d2; }
  `]
})
export class RegisterComponent {
  email = '';
  password = '';
  firstName = '';
  lastName = '';
  error = signal('');
  loading = signal(false);

  constructor(private authService: AuthService, private router: Router) {}

  onSubmit(): void {
    this.loading.set(true);
    this.error.set('');
    this.authService.register({ email: this.email, password: this.password, firstName: this.firstName, lastName: this.lastName }).subscribe({
      next: () => { this.router.navigate(['/login']); },
      error: (err) => {
        this.error.set(err.error?.message || 'Registration failed');
        this.loading.set(false);
      }
    });
  }
}
