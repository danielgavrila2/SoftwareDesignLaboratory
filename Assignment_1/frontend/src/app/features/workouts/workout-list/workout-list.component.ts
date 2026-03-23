import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { WorkoutService } from '../../../core/services/workout.service';
import { AuthService } from '../../../core/auth/auth.service';
import { Workout, WorkoutFilters, MuscleGroup, Difficulty, PageResponse } from '../../../shared/models/models';

@Component({
  selector: 'app-workout-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="container page">
      <!-- Header -->
      <div class="page-header flex items-center justify-between">
        <div>
          <h1 class="page-title">WORKOUTS</h1>
          <p class="page-subtitle">{{ result?.totalElements || 0 }} workouts available</p>
        </div>
        @if (auth.isUser()) {
          <a routerLink="/workouts/new" class="btn btn-primary btn-lg">+ Add Workout</a>
        }
      </div>

      <!-- Filters -->
      <div class="card filters-card">
        <div class="filters-grid">
          <div class="form-group">
            <label class="form-label">Search</label>
            <input class="form-control" type="text" placeholder="Search workouts..."
              [(ngModel)]="filters.name" (ngModelChange)="onFilterChange()" />
          </div>
          <div class="form-group">
            <label class="form-label">Muscle Group</label>
            <select class="form-control" [(ngModel)]="filters.muscleGroup" (ngModelChange)="onFilterChange()">
              <option value="">All Groups</option>
              @for (mg of muscleGroups; track mg) {
                <option [value]="mg">{{ mg | titlecase }}</option>
              }
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">Difficulty</label>
            <select class="form-control" [(ngModel)]="filters.difficulty" (ngModelChange)="onFilterChange()">
              <option value="">All Levels</option>
              @for (d of difficulties; track d) {
                <option [value]="d">{{ d | titlecase }}</option>
              }
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">Sort By</label>
            <select class="form-control" [(ngModel)]="filters.sortBy" (ngModelChange)="loadWorkouts()">
              <option value="createdAt">Date Added</option>
              <option value="name">Name</option>
              <option value="durationMinutes">Duration</option>
              <option value="caloriesBurned">Calories</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">Order</label>
            <select class="form-control" [(ngModel)]="filters.sortDir" (ngModelChange)="loadWorkouts()">
              <option value="desc">Newest First</option>
              <option value="asc">Oldest First</option>
            </select>
          </div>
          <div class="form-group" style="justify-content:flex-end;flex-direction:row;align-items:flex-end">
            <button class="btn btn-ghost" (click)="resetFilters()">Reset</button>
          </div>
        </div>
      </div>

      <!-- Loading -->
      @if (loading) {
        <div style="display:flex;justify-content:center;padding:60px">
          <div class="spinner"></div>
        </div>
      }

      <!-- Workout Grid -->
      @if (!loading && result) {
        @if (result.content.length === 0) {
          <div class="empty-state">
            <h3>No Workouts Found</h3>
            <p>Try adjusting your filters or add a new workout.</p>
          </div>
        } @else {
          <div class="workout-grid">
            @for (workout of result.content; track workout.id) {
              <div class="workout-card card">
                <div class="workout-card-header">
                  <span [class]="'badge badge-' + workout.difficulty.toLowerCase()">
                    {{ workout.difficulty }}
                  </span>
                  <span class="workout-duration">{{ workout.durationMinutes }} min</span>
                </div>
                <h3 class="workout-name">{{ workout.name }}</h3>
                <p class="workout-desc">{{ workout.description || 'No description provided.' }}</p>
                <div class="workout-meta">
                  <span class="meta-tag">💪 {{ formatMuscle(workout.muscleGroup) }}</span>
                  <span class="meta-tag">🔥 {{ workout.caloriesBurned }} kcal</span>
                </div>
                <div class="workout-footer">
                  <span class="workout-author">by {{ workout.createdByName }}</span>
                  @if (auth.isUser()) {
                    <div class="workout-actions">
                      <a [routerLink]="['/workouts', workout.id, 'edit']" class="btn btn-ghost btn-sm">Edit</a>
                      @if (auth.isAdmin()) {
                        <button class="btn btn-danger btn-sm" (click)="deleteWorkout(workout)">Delete</button>
                      }
                    </div>
                  }
                </div>
              </div>
            }
          </div>

          <!-- Pagination -->
          <div class="pagination-bar">
            <span class="pagination-info">
              Showing {{ result.page * result.size + 1 }}–{{ min(result.page * result.size + result.content.length, result.totalElements) }}
              of {{ result.totalElements }}
            </span>
            <div class="pagination">
              <button class="page-btn" [disabled]="result.page === 0" (click)="goToPage(0)">«</button>
              <button class="page-btn" [disabled]="result.page === 0" (click)="goToPage(result.page - 1)">‹</button>
              @for (p of getPages(result.totalPages); track p) {
                <button class="page-btn" [class.active]="p === result.page" (click)="goToPage(p)">{{ p + 1 }}</button>
              }
              <button class="page-btn" [disabled]="result.last" (click)="goToPage(result.page + 1)">›</button>
              <button class="page-btn" [disabled]="result.last" (click)="goToPage(result.totalPages - 1)">»</button>
            </div>
          </div>
        }
      }
    </div>
  `,
  styles: [`
    .filters-card { margin-bottom: 28px; }
    .filters-grid {
      display: grid;
      grid-template-columns: 2fr 1fr 1fr 1fr 1fr auto;
      gap: 16px;
      align-items: end;
    }
    .workout-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
      gap: 20px;
      margin-bottom: 32px;
    }
    .workout-card { display: flex; flex-direction: column; gap: 12px; cursor: default; }
    .workout-card:hover { border-color: var(--border-light); transform: translateY(-2px); box-shadow: var(--shadow); }
    .workout-card { transition: all 0.2s; }
    .workout-card-header { display: flex; align-items: center; justify-content: space-between; }
    .workout-duration { font-size: 13px; color: var(--text-muted); font-weight: 500; }
    .workout-name { font-family: var(--font-display); font-size: 22px; color: var(--text-primary); line-height: 1.2; }
    .workout-desc { font-size: 13px; color: var(--text-secondary); line-height: 1.5; flex: 1;
      display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
    .workout-meta { display: flex; gap: 10px; flex-wrap: wrap; }
    .meta-tag { font-size: 12px; color: var(--text-secondary); background: var(--bg-secondary);
      padding: 3px 10px; border-radius: 20px; border: 1px solid var(--border); }
    .workout-footer { display: flex; align-items: center; justify-content: space-between; padding-top: 8px; border-top: 1px solid var(--border); margin-top: auto; }
    .workout-author { font-size: 12px; color: var(--text-muted); }
    .workout-actions { display: flex; gap: 6px; }
    .pagination-bar { display: flex; align-items: center; justify-content: space-between; margin-top: 8px; }
    .pagination-info { font-size: 13px; color: var(--text-muted); }
    @media (max-width: 900px) {
      .filters-grid { grid-template-columns: 1fr 1fr; }
    }
    @media (max-width: 600px) {
      .filters-grid { grid-template-columns: 1fr; }
    }
  `]
})
export class WorkoutListComponent implements OnInit {
  result: PageResponse<Workout> | null = null;
  loading = false;

  filters: WorkoutFilters = {
    name: '', muscleGroup: '', difficulty: '',
    page: 0, size: 9, sortBy: 'createdAt', sortDir: 'desc'
  };

  muscleGroups: MuscleGroup[] = ['CHEST','BACK','SHOULDERS','ARMS','LEGS','CORE','FULL_BODY','CARDIO'];
  difficulties: Difficulty[] = ['BEGINNER','INTERMEDIATE','ADVANCED'];

  private searchTimer: any;

  constructor(public auth: AuthService, private workoutService: WorkoutService) {}

  ngOnInit() { this.loadWorkouts(); }

  loadWorkouts() {
    this.loading = true;
    this.workoutService.getWorkouts(this.filters).subscribe({
      next: (res) => { this.result = res; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  onFilterChange() {
    clearTimeout(this.searchTimer);
    this.searchTimer = setTimeout(() => {
      this.filters.page = 0;
      this.loadWorkouts();
    }, 400);
  }

  resetFilters() {
    this.filters = { name:'', muscleGroup:'', difficulty:'', page:0, size:9, sortBy:'createdAt', sortDir:'desc' };
    this.loadWorkouts();
  }

  goToPage(p: number) { this.filters.page = p; this.loadWorkouts(); }

  deleteWorkout(w: Workout) {
    if (!confirm(`Delete "${w.name}"?`)) return;
    this.workoutService.deleteWorkout(w.id).subscribe(() => this.loadWorkouts());
  }

  getPages(total: number): number[] {
    const current = this.filters.page;
    const start = Math.max(0, current - 2);
    const end = Math.min(total - 1, start + 4);
    return Array.from({ length: end - start + 1 }, (_, i) => start + i);
  }

  formatMuscle(mg: string): string {
    return mg.replace('_', ' ');
  }

  min(a: number, b: number): number { return Math.min(a, b); }
}
