import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../core/services/user.service';
import { User } from '../../shared/models/models';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container page">
      <div class="page-header">
        <h1 class="page-title">ADMIN PANEL</h1>
        <p class="page-subtitle">Manage users and system settings</p>
      </div>

      <!-- Stats row -->
      <div class="grid-4" style="margin-bottom:28px">
        <div class="card stat-card">
          <div class="stat-value">{{ users.length }}</div>
          <div class="stat-label">Total Users</div>
        </div>
        <div class="card stat-card">
          <div class="stat-value" style="color:var(--red)">{{ countRole('ADMIN') }}</div>
          <div class="stat-label">Admins</div>
        </div>
        <div class="card stat-card">
          <div class="stat-value" style="color:var(--blue)">{{ countRole('USER') }}</div>
          <div class="stat-label">Users</div>
        </div>
        <div class="card stat-card">
          <div class="stat-value" style="color:var(--text-muted)">{{ countDisabled() }}</div>
          <div class="stat-label">Disabled</div>
        </div>
      </div>

      <!-- Users table -->
      <div class="card" style="padding:0;overflow:hidden">
        <div style="padding:20px 24px;border-bottom:1px solid var(--border);display:flex;justify-content:space-between;align-items:center">
          <h2 style="font-size:20px">USER MANAGEMENT</h2>
          <input class="form-control" type="text" [(ngModel)]="search"
            placeholder="Filter users..." style="width:220px" />
        </div>

        @if (loading) {
          <div style="display:flex;justify-content:center;padding:40px">
            <div class="spinner"></div>
          </div>
        } @else {
          <table class="table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Email</th>
                <th>Role</th>
                <th>Status</th>
                <th>Joined</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              @for (user of filteredUsers(); track user.id) {
                <tr>
                  <td style="color:var(--text-muted)">#{{ user.id }}</td>
                  <td><strong>{{ user.firstName }} {{ user.lastName }}</strong></td>
                  <td style="color:var(--text-secondary)">{{ user.email }}</td>
                  <td>
                    <span [class]="'badge badge-' + getRoleBadge(user.role)">{{ user.role }}</span>
                  </td>
                  <td>
                    <span [class]="user.enabled ? 'status-active' : 'status-inactive'">
                      {{ user.enabled ? 'Active' : 'Disabled' }}
                    </span>
                  </td>
                  <td style="color:var(--text-muted);font-size:13px">{{ user.createdAt | date:'mediumDate' }}</td>
                  <td>
                    <div style="display:flex;gap:6px;flex-wrap:wrap">
                      <select class="form-control" style="width:110px;padding:5px 8px;font-size:12px"
                        [value]="user.role" (change)="changeRole(user, $event)">
                        <option value="USER">USER</option>
                        <option value="ADMIN">ADMIN</option>
                      </select>
                      <button class="btn btn-secondary btn-sm" (click)="toggle(user)">
                        {{ user.enabled ? 'Disable' : 'Enable' }}
                      </button>
                      <button class="btn btn-danger btn-sm" (click)="deleteUser(user)">Del</button>
                    </div>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        }
      </div>
    </div>
  `,
  styles: [`
    .status-active { font-size:12px; font-weight:600; color:var(--green); }
    .status-inactive { font-size:12px; font-weight:600; color:var(--text-muted); }
  `]
})
export class AdminComponent implements OnInit {
  users: User[] = [];
  loading = false;
  search = '';

  constructor(private userService: UserService) {}

  ngOnInit() {
    this.loading = true;
    this.userService.getAllUsers().subscribe({
      next: (u) => { this.users = u; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  filteredUsers(): User[] {
    if (!this.search) return this.users;
    const s = this.search.toLowerCase();
    return this.users.filter(u =>
      u.firstName.toLowerCase().includes(s) ||
      u.lastName.toLowerCase().includes(s) ||
      u.email.toLowerCase().includes(s)
    );
  }

  changeRole(user: User, event: Event) {
    const role = (event.target as HTMLSelectElement).value;
    this.userService.updateRole(user.id, role).subscribe(updated => {
      user.role = updated.role;
    });
  }

  toggle(user: User) {
    this.userService.toggleUser(user.id).subscribe(() => {
      user.enabled = !user.enabled;
    });
  }

  deleteUser(user: User) {
    if (!confirm(`Delete user ${user.email}?`)) return;
    this.userService.deleteUser(user.id).subscribe(() => {
      this.users = this.users.filter(u => u.id !== user.id);
    });
  }

  countRole(role: string): number { return this.users.filter(u => u.role === role).length; }
  countDisabled(): number { return this.users.filter(u => !u.enabled).length; }

  getRoleBadge(role: string): string {
    if (role === 'ADMIN') return 'advanced';
    if (role === 'USER') return 'intermediate';
    return 'beginner';
  }
}
