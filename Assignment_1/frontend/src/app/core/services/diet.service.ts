import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DietPlan } from '../../shared/models/models';

const BASE = 'http://localhost:8081/api/diet';

@Injectable({ providedIn: 'root' })
export class DietService {
  constructor(private http: HttpClient) {}

  getDietPlan(calories: number, goal: string): Observable<DietPlan> {
    const params = new HttpParams().set('calories', calories).set('goal', goal);
    return this.http.get<DietPlan>(`${BASE}/plan`, { params });
  }

  getNutrition(query: string): Observable<any> {
    const params = new HttpParams().set('query', query);
    return this.http.get<any>(`${BASE}/nutrition`, { params });
  }
}
