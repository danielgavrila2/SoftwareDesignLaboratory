import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/workouts', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent)
  },
  {
    path: 'workouts',
    loadComponent: () => import('./features/workouts/workout-list/workout-list.component').then(m => m.WorkoutListComponent)
  },
  {
    path: 'workouts/new',
    loadComponent: () => import('./features/workouts/workout-form/workout-form.component').then(m => m.WorkoutFormComponent),
    canActivate: [authGuard]
  },
  {
    path: 'workouts/:id/edit',
    loadComponent: () => import('./features/workouts/workout-form/workout-form.component').then(m => m.WorkoutFormComponent),
    canActivate: [authGuard]
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
    canActivate: [authGuard]
  },
  {
    path: 'diet',
    loadComponent: () => import('./features/diet/diet.component').then(m => m.DietComponent),
    canActivate: [authGuard]
  },
  {
    path: 'admin',
    loadComponent: () => import('./features/admin/admin.component').then(m => m.AdminComponent),
    canActivate: [adminGuard]
  },
  { path: '**', redirectTo: '/workouts' }
];
