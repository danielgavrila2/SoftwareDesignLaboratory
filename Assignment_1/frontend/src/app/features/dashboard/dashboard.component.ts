import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { WorkoutService } from '../../core/services/workout.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="container page">
      <div class="page-header">
        <h1 class="page-title">DASHBOARD</h1>
        <p class="page-subtitle">Welcome back, {{ auth.currentUser()?.fullName }}</p>
      </div>

      <!-- Role badge -->
      <div style="margin-bottom:28px">
        <span [class]="'badge badge-' + getRoleBadge()">
          {{ auth.currentUser()?.role }}
        </span>
      </div>

      <!-- Stats -->
      @if (stats) {
        <div class="grid-4" style="margin-bottom:32px">
          <div class="card stat-card">
            <div class="stat-value">{{ stats.totalWorkouts }}</div>
            <div class="stat-label">Total Workouts</div>
          </div>
          <div class="card stat-card">
            <div class="stat-value" style="color:var(--green)">{{ stats.byDifficulty?.BEGINNER || 0 }}</div>
            <div class="stat-label">Beginner</div>
          </div>
          <div class="card stat-card">
            <div class="stat-value" style="color:var(--orange)">{{ stats.byDifficulty?.INTERMEDIATE || 0 }}</div>
            <div class="stat-label">Intermediate</div>
          </div>
          <div class="card stat-card">
            <div class="stat-value" style="color:var(--red)">{{ stats.byDifficulty?.ADVANCED || 0 }}</div>
            <div class="stat-label">Advanced</div>
          </div>
        </div>

        <!-- Muscle group breakdown -->
        <div class="card" style="margin-bottom:28px">
          <h2 style="font-size:22px;margin-bottom:20px">WORKOUT BREAKDOWN</h2>
          <div class="breakdown-grid">
            @for (entry of getMuscleEntries(); track entry[0]) {
              <div class="breakdown-item">
                <div class="breakdown-bar-wrap">
                  <div class="breakdown-bar"
                    [style.width]="getPercent(entry[1]) + '%'">
                  </div>
                </div>
                <div class="breakdown-label">{{ formatMuscle(entry[0]) }}</div>
                <div class="breakdown-count">{{ entry[1] }}</div>
              </div>
            }
          </div>
        </div>
      }

      <!-- Quick Actions -->
      <div class="card">
        <h2 style="font-size:22px;margin-bottom:20px">QUICK ACTIONS</h2>
        <div class="actions-grid">
          <a routerLink="/workouts" class="action-card">
            <span class="action-icon">💪</span>
            <span class="action-label">Browse Workouts</span>
          </a>
          <a routerLink="/workouts/new" class="action-card">
            <span class="action-icon">➕</span>
            <span class="action-label">Add Workout</span>
          </a>
          <a routerLink="/diet" class="action-card">
            <span class="action-icon">🥗</span>
            <span class="action-label">Diet Plan</span>
          </a>
          @if (auth.isAdmin()) {
            <a routerLink="/admin" class="action-card action-card-admin">
              <span class="action-icon">⚙️</span>
              <span class="action-label">Admin Panel</span>
            </a>
          }
        </div>
      </div>
    </div>
  `,
  styles: [`
    .breakdown-grid { display: flex; flex-direction: column; gap: 12px; }
    .breakdown-item { display: grid; grid-template-columns: 1fr auto; gap: 12px; align-items: center; }
    .breakdown-bar-wrap { height: 8px; background: var(--bg-secondary); border-radius: 4px; overflow: hidden; grid-column: 1; }
    .breakdown-item { display: grid; grid-template-columns: 1fr 120px 40px; align-items: center; gap: 12px; }
    .breakdown-bar { height: 8px; background: var(--accent); border-radius: 4px; transition: width 0.6s ease; min-width: 4px; }
    .breakdown-label { font-size: 13px; color: var(--text-secondary); font-weight: 500; }
    .breakdown-count { font-size: 13px; color: var(--text-primary); font-weight: 700; text-align: right; }
    .actions-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 16px; }
    .action-card {
      display: flex; flex-direction: column; align-items: center; gap: 10px;
      padding: 24px 16px; border-radius: var(--radius); border: 1px solid var(--border);
      background: var(--bg-secondary); text-decoration: none; color: var(--text-primary);
      transition: all 0.2s; cursor: pointer;
    }
    .action-card:hover { border-color: var(--accent); background: var(--accent-dim); transform: translateY(-2px); }
    .action-card-admin:hover { border-color: var(--red); background: rgba(255,71,87,0.08); }
    .action-icon { font-size: 28px; }
    .action-label { font-size: 13px; font-weight: 600; color: var(--text-secondary); text-align: center; }
  `]
})
export class DashboardComponent implements OnInit {
  stats: any = null;

  constructor(public auth: AuthService, private workoutService: WorkoutService) {}

  ngOnInit() {
    this.workoutService.getStats().subscribe(s => this.stats = s);
  }

  getMuscleEntries(): [string, number][] {
    if (!this.stats?.byMuscleGroup) return [];
    return Object.entries(this.stats.byMuscleGroup) as [string, number][];
  }

  getPercent(count: number): number {
    if (!this.stats?.totalWorkouts) return 0;
    return Math.round((count / this.stats.totalWorkouts) * 100);
  }

  formatMuscle(s: string): string { return s.replace('_', ' '); }

  getRoleBadge(): string {
    const role = this.auth.currentUser()?.role;
    if (role === 'ADMIN') return 'advanced';
    if (role === 'USER') return 'intermediate';
    return 'beginner';
  }
}
