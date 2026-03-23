import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PageResponse, Workout, WorkoutFilters, WorkoutRequest } from '../../shared/models/models';

const API = 'http://localhost:8081/api/workouts';

@Injectable({ providedIn: 'root' })
export class WorkoutService {
  constructor(private http: HttpClient) {}

  getWorkouts(filters: WorkoutFilters): Observable<PageResponse<Workout>> {
    let params = new HttpParams()
      .set('page', filters.page)
      .set('size', filters.size)
      .set('sortBy', filters.sortBy)
      .set('sortDir', filters.sortDir);

    if (filters.name) params = params.set('name', filters.name);
    if (filters.muscleGroup) params = params.set('muscleGroup', filters.muscleGroup);
    if (filters.difficulty) params = params.set('difficulty', filters.difficulty);
    if (filters.minDuration) params = params.set('minDuration', filters.minDuration);
    if (filters.maxDuration) params = params.set('maxDuration', filters.maxDuration);

    return this.http.get<PageResponse<Workout>>(API, { params });
  }

  getWorkout(id: number): Observable<Workout> {
    return this.http.get<Workout>(`${API}/${id}`);
  }

  createWorkout(req: WorkoutRequest): Observable<Workout> {
    return this.http.post<Workout>(API, req);
  }

  updateWorkout(id: number, req: WorkoutRequest): Observable<Workout> {
    return this.http.put<Workout>(`${API}/${id}`, req);
  }

  deleteWorkout(id: number): Observable<void> {
    return this.http.delete<void>(`${API}/${id}`);
  }

  getStats(): Observable<any> {
    return this.http.get<any>(`${API}/stats`);
  }
}
