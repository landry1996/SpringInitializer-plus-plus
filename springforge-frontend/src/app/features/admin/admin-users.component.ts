import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from './admin.service';
import { AdminUser } from './admin.model';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="admin-users">
      <div class="header">
        <h3>User Management</h3>
        <button (click)="showCreateForm = !showCreateForm">+ New User</button>
      </div>

      <div class="create-form" *ngIf="showCreateForm">
        <input [(ngModel)]="newUser.username" placeholder="Username">
        <input [(ngModel)]="newUser.email" placeholder="Email">
        <select [(ngModel)]="newUser.role">
          <option value="USER">USER</option>
          <option value="ADMIN">ADMIN</option>
          <option value="VIEWER">VIEWER</option>
        </select>
        <button (click)="createUser()">Create</button>
      </div>

      <table class="users-table">
        <thead>
          <tr>
            <th>Username</th>
            <th>Email</th>
            <th>Role</th>
            <th>Status</th>
            <th>Last Login</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let user of users">
            <td>{{ user.username }}</td>
            <td>{{ user.email }}</td>
            <td>
              <select [ngModel]="user.role" (ngModelChange)="changeRole(user.id, $event)">
                <option value="ADMIN">ADMIN</option>
                <option value="USER">USER</option>
                <option value="VIEWER">VIEWER</option>
              </select>
            </td>
            <td><span [class]="user.active ? 'badge-active' : 'badge-inactive'">{{ user.active ? 'Active' : 'Inactive' }}</span></td>
            <td>{{ user.lastLogin || 'Never' }}</td>
            <td>
              <button class="btn-danger" (click)="deactivate(user.id)" *ngIf="user.active">Deactivate</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  `,
  styles: [`
    .admin-users { padding: 1rem; }
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
    .header button { background: #4CAF50; color: white; border: none; padding: 0.5rem 1rem; border-radius: 4px; cursor: pointer; }
    .create-form { display: flex; gap: 0.5rem; margin-bottom: 1rem; padding: 1rem; background: #f5f5f5; border-radius: 4px; }
    .create-form input, .create-form select { padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px; }
    .create-form button { background: #1976d2; color: white; border: none; padding: 0.5rem 1rem; border-radius: 4px; cursor: pointer; }
    .users-table { width: 100%; border-collapse: collapse; }
    .users-table th, .users-table td { padding: 0.75rem; text-align: left; border-bottom: 1px solid #e0e0e0; }
    .users-table th { background: #f5f5f5; font-weight: 600; }
    .badge-active { color: #2e7d32; background: #e8f5e9; padding: 2px 8px; border-radius: 4px; }
    .badge-inactive { color: #c62828; background: #ffebee; padding: 2px 8px; border-radius: 4px; }
    .btn-danger { background: #dc3545; color: white; border: none; padding: 4px 8px; border-radius: 4px; cursor: pointer; font-size: 0.85rem; }
    select { padding: 4px; border: 1px solid #ddd; border-radius: 4px; }
  `]
})
export class AdminUsersComponent implements OnInit {
  users: AdminUser[] = [];
  showCreateForm = false;
  newUser: Partial<AdminUser> = { role: 'USER' };

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.adminService.getUsers().subscribe(page => this.users = page.content);
  }

  createUser(): void {
    this.adminService.createUser(this.newUser).subscribe(() => {
      this.loadUsers();
      this.showCreateForm = false;
      this.newUser = { role: 'USER' };
    });
  }

  changeRole(id: string, role: string): void {
    this.adminService.changeRole(id, role).subscribe();
  }

  deactivate(id: string): void {
    this.adminService.deactivateUser(id).subscribe(() => this.loadUsers());
  }
}
