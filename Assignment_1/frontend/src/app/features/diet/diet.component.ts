import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DietService } from '../../core/services/diet.service';
import { DietPlan } from '../../shared/models/models';

@Component({
  selector: 'app-diet',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container page">
      <div class="page-header">
        <h1 class="page-title">DIET PLANNER</h1>
        <p class="page-subtitle">Generate a personalised meal plan based on your goals</p>
      </div>

      <!-- Generator Form -->
      <div class="card" style="max-width:600px;margin-bottom:32px">
        <h2 style="font-size:22px;margin-bottom:20px">Generate Plan</h2>
        <div class="form-group">
          <label class="form-label">Daily Calorie Target</label>
          <input class="form-control" type="number" [(ngModel)]="calories"
            min="1000" max="5000" placeholder="2000" />
        </div>
        <div class="form-group" style="margin-top:14px">
          <label class="form-label">Goal</label>
          <select class="form-control" [(ngModel)]="goal">
            <option value="maintenance">Maintenance</option>
            <option value="weight_loss">Weight Loss</option>
            <option value="muscle_gain">Muscle Gain</option>
          </select>
        </div>
        <button class="btn btn-primary btn-lg" style="margin-top:20px;width:100%"
          (click)="generate()" [disabled]="loading">
          {{ loading ? 'Generating...' : '🥗 Generate My Plan' }}
        </button>
      </div>

      <!-- Nutrition Lookup -->
      <div class="card" style="max-width:600px;margin-bottom:32px">
        <h2 style="font-size:22px;margin-bottom:20px">Nutrition Lookup</h2>
        <div style="display:flex;gap:10px">
          <input class="form-control" type="text" [(ngModel)]="nutritionQuery"
            placeholder="e.g. 100g chicken breast" style="flex:1" />
          <button class="btn btn-secondary" (click)="lookupNutrition()" [disabled]="nutritionLoading">
            Look Up
          </button>
        </div>
        @if (nutritionResult) {
          <div class="nutrition-result">
            <h4>{{ nutritionResult.name || nutritionQuery }}</h4>
            <div class="macro-pills">
              <div class="macro-pill"><span class="macro-val">{{ nutritionResult.calories | number:'1.0-0' }}</span><span class="macro-key">kcal</span></div>
              <div class="macro-pill"><span class="macro-val">{{ nutritionResult.protein_g | number:'1.0-0' }}g</span><span class="macro-key">Protein</span></div>
              <div class="macro-pill"><span class="macro-val">{{ nutritionResult.carbs_g | number:'1.0-0' }}g</span><span class="macro-key">Carbs</span></div>
              <div class="macro-pill"><span class="macro-val">{{ nutritionResult.fat_g | number:'1.0-0' }}g</span><span class="macro-key">Fat</span></div>
            </div>
            @if (nutritionResult.note) {
              <p style="font-size:12px;color:var(--text-muted);margin-top:8px">ℹ️ {{ nutritionResult.note }}</p>
            }
          </div>
        }
      </div>

      <!-- Generated Plan -->
      @if (plan) {
        <div class="plan-section">
          <div class="plan-header">
            <h2 style="font-size:28px">YOUR PLAN</h2>
            <div class="plan-meta">
              <span class="badge badge-intermediate">{{ plan.goal | titlecase }}</span>
              <span style="color:var(--text-secondary);font-size:14px">
                Target: <strong style="color:var(--accent)">{{ plan.targetCalories }}</strong> kcal/day
              </span>
            </div>
          </div>

          <!-- Macro split -->
          <div class="card" style="margin-bottom:24px">
            <h3 style="font-size:18px;margin-bottom:16px">MACRO SPLIT</h3>
            <div class="macro-bar">
              <div class="macro-seg macro-protein" [style.flex]="plan.macroSplit.protein">
                Protein {{ plan.macroSplit.protein }}%
              </div>
              <div class="macro-seg macro-carbs" [style.flex]="plan.macroSplit.carbs">
                Carbs {{ plan.macroSplit.carbs }}%
              </div>
              <div class="macro-seg macro-fat" [style.flex]="plan.macroSplit.fat">
                Fat {{ plan.macroSplit.fat }}%
              </div>
            </div>
          </div>

          <!-- Meals -->
          <div class="meals-grid">
            @for (meal of plan.meals; track meal.mealType) {
              <div class="meal-card card">
                <div class="meal-type">{{ meal.mealType }}</div>
                <div class="meal-name">{{ meal.name }}</div>
                <div class="meal-cals">{{ meal.calories }} kcal</div>
                <div class="meal-macros">
                  <span>P: {{ meal.protein_g }}g</span>
                  <span>C: {{ meal.carbs_g }}g</span>
                  <span>F: {{ meal.fat_g }}g</span>
                </div>
              </div>
            }
          </div>

          <!-- Tips -->
          <div class="card" style="margin-top:24px">
            <h3 style="font-size:18px;margin-bottom:14px">💡 TIPS</h3>
            <ul class="tips-list">
              @for (tip of plan.tips; track tip) {
                <li>{{ tip }}</li>
              }
            </ul>
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    .plan-header { display:flex; align-items:center; justify-content:space-between; margin-bottom:24px; }
    .plan-meta { display:flex; align-items:center; gap:12px; }
    .macro-bar { display:flex; height:40px; border-radius: var(--radius-sm); overflow:hidden; gap:2px; }
    .macro-seg { display:flex; align-items:center; justify-content:center; font-size:12px; font-weight:700; color:#000; transition:flex 0.5s; }
    .macro-protein { background: var(--accent); }
    .macro-carbs { background: var(--orange); }
    .macro-fat { background: var(--blue); color: #fff; }
    .meals-grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(220px,1fr)); gap:16px; }
    .meal-card { display:flex; flex-direction:column; gap:8px; }
    .meal-type { font-size:11px; text-transform:uppercase; letter-spacing:0.1em; color:var(--text-muted); font-weight:600; }
    .meal-name { font-size:15px; font-weight:600; color:var(--text-primary); line-height:1.3; }
    .meal-cals { font-family:var(--font-display); font-size:28px; color:var(--accent); }
    .meal-macros { display:flex; gap:12px; font-size:12px; color:var(--text-muted); }
    .tips-list { list-style:none; display:flex; flex-direction:column; gap:10px; }
    .tips-list li { font-size:14px; color:var(--text-secondary); padding-left:20px; position:relative; }
    .tips-list li::before { content:'→'; position:absolute; left:0; color:var(--accent); font-weight:700; }
    .nutrition-result { margin-top:16px; padding:16px; background:var(--bg-secondary); border-radius:var(--radius-sm); }
    .nutrition-result h4 { font-size:15px; font-weight:600; margin-bottom:12px; color:var(--text-primary); }
    .macro-pills { display:flex; gap:10px; flex-wrap:wrap; }
    .macro-pill { display:flex; flex-direction:column; align-items:center; background:var(--bg-card); border:1px solid var(--border); border-radius:var(--radius-sm); padding:8px 14px; }
    .macro-val { font-family:var(--font-display); font-size:20px; color:var(--accent); }
    .macro-key { font-size:11px; color:var(--text-muted); text-transform:uppercase; letter-spacing:0.08em; }
  `]
})
export class DietComponent {
  calories = 2000;
  goal = 'maintenance';
  loading = false;
  plan: DietPlan | null = null;

  nutritionQuery = '';
  nutritionLoading = false;
  nutritionResult: any = null;

  constructor(private dietService: DietService) {}

  generate() {
    this.loading = true;
    this.dietService.getDietPlan(this.calories, this.goal).subscribe({
      next: (p) => { this.plan = p; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  lookupNutrition() {
    if (!this.nutritionQuery.trim()) return;
    this.nutritionLoading = true;
    this.dietService.getNutrition(this.nutritionQuery).subscribe({
      next: (r) => { this.nutritionResult = r; this.nutritionLoading = false; },
      error: () => { this.nutritionLoading = false; }
    });
  }
}
