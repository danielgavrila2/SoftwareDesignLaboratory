import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="auth-page">
      <div class="auth-card card">
        <div class="auth-header">
          <div class="auth-logo">⚡</div>
          <h1>WELCOME BACK</h1>
          <p>Sign in to continue your fitness journey</p>
        </div>

        @if (error) {
          <div class="alert alert-error">{{ error }}</div>
        }

        <form (ngSubmit)="onSubmit()" #f="ngForm">
          <div class="form-group">
            <label class="form-label">Email</label>
            <input class="form-control" type="email" name="email"
              [(ngModel)]="email" required placeholder="you@example.com" />
          </div>

          <div class="form-group" style="margin-top:16px">
            <label class="form-label">Password</label>
            <input class="form-control" type="password" name="password"
              [(ngModel)]="password" required placeholder="••••••••" />
          </div>

          <button class="btn btn-primary btn-full btn-lg" style="margin-top:24px"
            type="submit" [disabled]="loading">
            {{ loading ? 'Signing in...' : 'Sign In' }}
          </button>
        </form>

        <div class="auth-footer">
          <p>Don't have an account? <a routerLink="/register">Get Started</a></p>
        </div>

        <div class="demo-creds">
          <p class="demo-title">Demo Credentials</p>
          <div class="demo-row" (click)="fillAdmin()">
            <span class="badge badge-advanced">Admin</span>
            <span>admin&#64;fitcore.com / admin123</span>
          </div>
          <div class="demo-row" (click)="fillUser()">
            <span class="badge badge-beginner">User</span>
            <span>alex&#64;example.com / user123</span>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .auth-page {
      min-height: calc(100vh - 64px);
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 32px 16px;
      background: radial-gradient(ellipse at 50% 0%, rgba(232,255,0,0.04) 0%, transparent 70%);
    }
    .auth-card {
      width: 100%;
      max-width: 420px;
    }
    .auth-header {
      text-align: center;
      margin-bottom: 32px;
    }
    .auth-logo {
      font-size: 40px;
      margin-bottom: 12px;
    }
    .auth-header h1 {
      font-size: 36px;
      color: var(--text-primary);
      margin-bottom: 6px;
    }
    .auth-header p { color: var(--text-secondary); font-size: 14px; }
    .auth-footer {
      text-align: center;
      margin-top: 24px;
      font-size: 14px;
      color: var(--text-secondary);
    }
    .auth-footer a { color: var(--accent); text-decoration: none; font-weight: 600; }
    .demo-creds {
      margin-top: 24px;
      padding: 16px;
      background: var(--bg-secondary);
      border-radius: var(--radius-sm);
      border: 1px solid var(--border);
    }
    .demo-title {
      font-size: 11px;
      text-transform: uppercase;
      letter-spacing: 0.1em;
      color: var(--text-muted);
      margin-bottom: 10px;
      font-weight: 600;
    }
    .demo-row {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 6px 0;
      font-size: 13px;
      color: var(--text-secondary);
      cursor: pointer;
      border-radius: 4px;
    }
    .demo-row:hover { color: var(--text-primary); }
  `]
})
export class LoginComponent {
  email = '';
  password = '';
  loading = false;
  error = '';

  constructor(private auth: AuthService, private router: Router) {}

  fillAdmin() { this.email = 'admin@fitcore.com'; this.password = 'admin123'; }
  fillUser() { this.email = 'alex@example.com'; this.password = 'user123'; }

  onSubmit() {
    if (!this.email || !this.password) return;
    this.loading = true;
    this.error = '';
    this.auth.login({ email: this.email, password: this.password }).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: (err) => {
        this.error = err.error?.message || 'Invalid email or password';
        this.loading = false;
      }
    });
  }
}
