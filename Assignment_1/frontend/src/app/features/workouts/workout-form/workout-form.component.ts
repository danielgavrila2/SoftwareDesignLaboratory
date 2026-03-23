import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { WorkoutService } from '../../../core/services/workout.service';
import { WorkoutRequest, MuscleGroup, Difficulty } from '../../../shared/models/models';

@Component({
  selector: 'app-workout-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="container page">
      <div class="page-header">
        <a routerLink="/workouts" class="btn btn-ghost btn-sm" style="margin-bottom:16px">← Back</a>
        <h1 class="page-title">{{ isEdit ? 'EDIT WORKOUT' : 'NEW WORKOUT' }}</h1>
      </div>

      @if (error) {
        <div class="alert alert-error" style="margin-bottom:20px">{{ error }}</div>
      }

      <div class="card" style="max-width:680px">
        <form (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label class="form-label">Workout Name *</label>
            <input class="form-control" type="text" [(ngModel)]="form.name"
              name="name" required placeholder="e.g. Push-Up Power" />
          </div>

          <div class="form-group" style="margin-top:16px">
            <label class="form-label">Description</label>
            <textarea class="form-control" [(ngModel)]="form.description"
              name="description" rows="3" placeholder="Describe this workout..."></textarea>
          </div>

          <div class="grid-2" style="margin-top:16px;gap:16px">
            <div class="form-group">
              <label class="form-label">Muscle Group *</label>
              <select class="form-control" [(ngModel)]="form.muscleGroup" name="muscleGroup" required>
                <option value="">Select...</option>
                @for (mg of muscleGroups; track mg) {
                  <option [value]="mg">{{ formatEnum(mg) }}</option>
                }
              </select>
            </div>
            <div class="form-group">
              <label class="form-label">Difficulty *</label>
              <select class="form-control" [(ngModel)]="form.difficulty" name="difficulty" required>
                <option value="">Select...</option>
                @for (d of difficulties; track d) {
                  <option [value]="d">{{ d | titlecase }}</option>
                }
              </select>
            </div>
          </div>

          <div class="grid-2" style="margin-top:16px;gap:16px">
            <div class="form-group">
              <label class="form-label">Duration (minutes) *</label>
              <input class="form-control" type="number" [(ngModel)]="form.durationMinutes"
                name="durationMinutes" required min="1" max="300" placeholder="30" />
            </div>
            <div class="form-group">
              <label class="form-label">Calories Burned</label>
              <input class="form-control" type="number" [(ngModel)]="form.caloriesBurned"
                name="caloriesBurned" min="0" placeholder="250" />
            </div>
          </div>

          <div class="form-actions">
            <a routerLink="/workouts" class="btn btn-secondary">Cancel</a>
            <button type="submit" class="btn btn-primary" [disabled]="loading">
              {{ loading ? 'Saving...' : (isEdit ? 'Update Workout' : 'Create Workout') }}
            </button>
          </div>
        </form>
      </div>
    </div>
  `,
  styles: [`
    textarea.form-control { resize: vertical; min-height: 80px; }
    .form-actions { display: flex; gap: 12px; justify-content: flex-end; margin-top: 28px; padding-top: 20px; border-top: 1px solid var(--border); }
  `]
})
export class WorkoutFormComponent implements OnInit {
  isEdit = false;
  workoutId: number | null = null;
  loading = false;
  error = '';

  form: WorkoutRequest = {
    name: '', description: '',
    muscleGroup: '' as MuscleGroup,
    difficulty: '' as Difficulty,
    durationMinutes: 30, caloriesBurned: 0
  };

  muscleGroups: MuscleGroup[] = ['CHEST','BACK','SHOULDERS','ARMS','LEGS','CORE','FULL_BODY','CARDIO'];
  difficulties: Difficulty[] = ['BEGINNER','INTERMEDIATE','ADVANCED'];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private workoutService: WorkoutService
  ) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit = true;
      this.workoutId = +id;
      this.workoutService.getWorkout(this.workoutId).subscribe({
        next: (w) => {
          this.form = {
            name: w.name, description: w.description,
            muscleGroup: w.muscleGroup, difficulty: w.difficulty,
            durationMinutes: w.durationMinutes, caloriesBurned: w.caloriesBurned
          };
        },
        error: () => this.router.navigate(['/workouts'])
      });
    }
  }

  onSubmit() {
    this.loading = true;
    this.error = '';
    const call = this.isEdit
      ? this.workoutService.updateWorkout(this.workoutId!, this.form)
      : this.workoutService.createWorkout(this.form);

    call.subscribe({
      next: () => this.router.navigate(['/workouts']),
      error: (err) => {
        this.error = err.error?.message || 'Failed to save workout';
        this.loading = false;
      }
    });
  }

  formatEnum(s: string): string {
    return s.replace('_', ' ');
  }
}
