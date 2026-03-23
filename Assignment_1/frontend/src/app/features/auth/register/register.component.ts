import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="auth-page">
      <div class="auth-card card">
        <div class="auth-header">
          <div class="auth-logo">⚡</div>
          <h1>JOIN FITCORE</h1>
          <p>Start your transformation today</p>
        </div>

        @if (error) {
          <div class="alert alert-error">{{ error }}</div>
        }

        <form (ngSubmit)="onSubmit()">
          <div class="grid-2" style="gap:12px">
            <div class="form-group">
              <label class="form-label">First Name</label>
              <input class="form-control" type="text" name="firstName"
                [(ngModel)]="form.firstName" required placeholder="Alex" />
            </div>
            <div class="form-group">
              <label class="form-label">Last Name</label>
              <input class="form-control" type="text" name="lastName"
                [(ngModel)]="form.lastName" required placeholder="Johnson" />
            </div>
          </div>

          <div class="form-group" style="margin-top:14px">
            <label class="form-label">Email</label>
            <input class="form-control" type="email" name="email"
              [(ngModel)]="form.email" required placeholder="you@example.com" />
          </div>

          <div class="form-group" style="margin-top:14px">
            <label class="form-label">Password</label>
            <input class="form-control" type="password" name="password"
              [(ngModel)]="form.password" required minlength="6" placeholder="Min 6 characters" />
          </div>

          <button class="btn btn-primary btn-full btn-lg" style="margin-top:24px"
            type="submit" [disabled]="loading">
            {{ loading ? 'Creating account...' : 'Create Account' }}
          </button>
        </form>

        <div class="auth-footer">
          <p>Already have an account? <a routerLink="/login">Sign In</a></p>
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
    .auth-card { width: 100%; max-width: 440px; }
    .auth-header { text-align: center; margin-bottom: 32px; }
    .auth-logo { font-size: 40px; margin-bottom: 12px; }
    .auth-header h1 { font-size: 36px; margin-bottom: 6px; }
    .auth-header p { color: var(--text-secondary); font-size: 14px; }
    .auth-footer { text-align: center; margin-top: 24px; font-size: 14px; color: var(--text-secondary); }
    .auth-footer a { color: var(--accent); text-decoration: none; font-weight: 600; }
  `]
})
export class RegisterComponent {
  form = { firstName: '', lastName: '', email: '', password: '' };
  loading = false;
  error = '';

  constructor(private auth: AuthService, private router: Router) {}

  onSubmit() {
    this.loading = true;
    this.error = '';
    this.auth.register(this.form).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: (err) => {
        this.error = err.error?.message || 'Registration failed';
        this.loading = false;
      }
    });
  }
}
