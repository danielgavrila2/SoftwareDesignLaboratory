import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../../shared/models/models';

const BASE = 'http://localhost:8081/api';

@Injectable({ providedIn: 'root' })
export class UserService {
  constructor(private http: HttpClient) {}

  getMe(): Observable<User> {
    return this.http.get<User>(`${BASE}/user/me`);
  }

  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${BASE}/admin/users`);
  }

  updateRole(id: number, role: string): Observable<User> {
    return this.http.put<User>(`${BASE}/admin/users/${id}/role`, null, { params: { role } });
  }

  toggleUser(id: number): Observable<void> {
    return this.http.put<void>(`${BASE}/admin/users/${id}/toggle`, null);
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${BASE}/admin/users/${id}`);
  }
}
