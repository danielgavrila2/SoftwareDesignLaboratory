import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from './core/auth/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule],
  template: `
    <nav class="navbar">
      <div class="container nav-inner">
        <a routerLink="/" class="nav-brand">
          <span class="brand-icon">⚡</span>
          <span class="brand-text">FITCORE</span>
        </a>

        <div class="nav-links">
          <a routerLink="/workouts" routerLinkActive="active" class="nav-link">Workouts</a>
          @if (auth.isLoggedIn()) {
            <a routerLink="/dashboard" routerLinkActive="active" class="nav-link">Dashboard</a>
            <a routerLink="/diet" routerLinkActive="active" class="nav-link">Diet Plan</a>
          }
          @if (auth.isAdmin()) {
            <a routerLink="/admin" routerLinkActive="active" class="nav-link nav-link-admin">Admin</a>
          }
        </div>

        <div class="nav-actions">
          @if (auth.isLoggedIn()) {
            <span class="nav-user">{{ auth.currentUser()?.fullName }}</span>
            <button class="btn btn-secondary btn-sm" (click)="logout()">Sign Out</button>
          } @else {
            <a routerLink="/login" class="btn btn-ghost btn-sm">Sign In</a>
            <a routerLink="/register" class="btn btn-primary btn-sm">Get Started</a>
          }
        </div>
      </div>
    </nav>

    <main>
      <router-outlet />
    </main>
  `,
  styles: [`
    .navbar {
      position: sticky;
      top: 0;
      z-index: 100;
      background: rgba(10,10,15,0.9);
      backdrop-filter: blur(12px);
      border-bottom: 1px solid var(--border);
    }
    .nav-inner {
      display: flex;
      align-items: center;
      justify-content: space-between;
      height: 64px;
    }
    .nav-brand {
      display: flex;
      align-items: center;
      gap: 8px;
      text-decoration: none;
    }
    .brand-icon { font-size: 22px; }
    .brand-text {
      font-family: var(--font-display);
      font-size: 26px;
      color: var(--accent);
      letter-spacing: 0.06em;
    }
    .nav-links {
      display: flex;
      align-items: center;
      gap: 4px;
    }
    .nav-link {
      padding: 6px 14px;
      border-radius: var(--radius-sm);
      color: var(--text-secondary);
      text-decoration: none;
      font-size: 14px;
      font-weight: 500;
      transition: all 0.2s;
    }
    .nav-link:hover, .nav-link.active { color: var(--text-primary); background: var(--bg-card); }
    .nav-link-admin { color: var(--accent) !important; }
    .nav-actions { display: flex; align-items: center; gap: 10px; }
    .nav-user { font-size: 13px; color: var(--text-secondary); font-weight: 500; }
    main { min-height: calc(100vh - 64px); }
  `]
})
export class AppComponent {
  constructor(public auth: AuthService, private router: Router) {}

  logout() {
    this.auth.logout();
    this.router.navigate(['/workouts']);
  }
}
