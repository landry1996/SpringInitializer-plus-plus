import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WebhookService } from './webhook.service';
import { WebhookConfig, CreateWebhookRequest, DeliveryLog, EVENT_TYPES } from './webhook.model';

@Component({
  selector: 'app-webhooks',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="webhooks-page">
      <div class="header">
        <h2>Webhooks & Notifications</h2>
        <button class="btn-primary" (click)="showForm = true; resetForm()">+ New Webhook</button>
      </div>

      <div class="webhook-list">
        <div *ngFor="let wh of webhooks" class="webhook-card">
          <div class="webhook-info">
            <div class="webhook-name">{{ wh.name }}</div>
            <div class="webhook-url">{{ wh.url | slice:0:50 }}{{ wh.url.length > 50 ? '...' : '' }}</div>
            <span class="channel-badge" [class]="wh.channel.toLowerCase()">{{ wh.channel }}</span>
            <span class="events-count">{{ wh.events.length }} events</span>
          </div>
          <div class="webhook-actions">
            <label class="toggle">
              <input type="checkbox" [checked]="wh.active" (change)="toggleActive(wh)">
              <span class="slider"></span>
            </label>
            <button class="btn-icon" (click)="testWebhook(wh)" title="Test">&#9889;</button>
            <button class="btn-icon" (click)="editWebhook(wh)" title="Edit">&#9998;</button>
            <button class="btn-icon btn-danger" (click)="deleteWebhook(wh)" title="Delete">&#10005;</button>
            <button class="btn-icon" (click)="toggleDeliveries(wh)" title="History">&#128203;</button>
          </div>
          <div *ngIf="testResult[wh.id]" class="test-result" [class.success]="testResult[wh.id] === 'success'" [class.error]="testResult[wh.id] === 'error'">
            {{ testResult[wh.id] === 'success' ? 'Test sent successfully!' : 'Test failed!' }}
          </div>
          <div *ngIf="expandedWebhook === wh.id" class="deliveries-panel">
            <table class="deliveries-table">
              <thead>
                <tr><th>Event</th><th>Status</th><th>Attempts</th><th>Date</th></tr>
              </thead>
              <tbody>
                <tr *ngFor="let d of deliveries">
                  <td>{{ d.eventType }}</td>
                  <td><span class="status-dot" [class.success]="d.success" [class.fail]="!d.success"></span> {{ d.httpStatus || '-' }}</td>
                  <td>{{ d.attemptCount }}</td>
                  <td>{{ d.createdAt | date:'short' }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
        <div *ngIf="webhooks.length === 0" class="empty-state">
          No webhooks configured. Click "+ New Webhook" to get started.
        </div>
      </div>

      <div *ngIf="showForm" class="form-overlay" (click)="showForm = false">
        <div class="form-panel" (click)="$event.stopPropagation()">
          <h3>{{ editingId ? 'Edit' : 'Create' }} Webhook</h3>
          <div class="form-group">
            <label>Name</label>
            <input [(ngModel)]="formName" placeholder="My Webhook">
          </div>
          <div class="form-group">
            <label>URL</label>
            <input [(ngModel)]="formUrl" placeholder="https://example.com/webhook">
          </div>
          <div class="form-group">
            <label>Secret Token (optional)</label>
            <input [(ngModel)]="formSecret" placeholder="whsec_..." type="password">
          </div>
          <div class="form-group">
            <label>Channel</label>
            <select [(ngModel)]="formChannel">
              <option value="WEBHOOK">Webhook (HTTP POST)</option>
              <option value="SLACK">Slack</option>
              <option value="EMAIL">Email</option>
            </select>
          </div>
          <div class="form-group">
            <label>Events</label>
            <div class="event-checkboxes">
              <label *ngFor="let evt of eventTypes" class="checkbox-label">
                <input type="checkbox" [checked]="formEvents.includes(evt)" (change)="toggleEvent(evt)">
                {{ evt.replace('_', ' ') | titlecase }}
              </label>
            </div>
          </div>
          <div class="form-actions">
            <button class="btn-cancel" (click)="showForm = false">Cancel</button>
            <button class="btn-primary" (click)="saveWebhook()">{{ editingId ? 'Update' : 'Create' }}</button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .webhooks-page { padding: 2rem; max-width: 900px; margin: 0 auto; }
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
    .btn-primary { padding: 0.6rem 1.2rem; background: #1976d2; color: white; border: none; border-radius: 6px; cursor: pointer; font-weight: 500; }
    .webhook-list { display: flex; flex-direction: column; gap: 1rem; }
    .webhook-card { border: 1px solid #e0e0e0; border-radius: 8px; padding: 1.25rem; }
    .webhook-info { display: flex; align-items: center; gap: 1rem; flex-wrap: wrap; }
    .webhook-name { font-weight: 600; font-size: 1rem; }
    .webhook-url { color: #666; font-size: 0.85rem; font-family: monospace; }
    .channel-badge { padding: 3px 8px; border-radius: 4px; font-size: 0.75rem; font-weight: bold; }
    .channel-badge.webhook { background: #e3f2fd; color: #1565c0; }
    .channel-badge.slack { background: #e8f5e9; color: #2e7d32; }
    .channel-badge.email { background: #fce4ec; color: #c62828; }
    .events-count { font-size: 0.8rem; color: #888; }
    .webhook-actions { display: flex; align-items: center; gap: 0.5rem; margin-top: 0.75rem; }
    .btn-icon { background: none; border: 1px solid #ddd; border-radius: 4px; padding: 4px 8px; cursor: pointer; font-size: 1rem; }
    .btn-icon:hover { background: #f5f5f5; }
    .btn-icon.btn-danger:hover { background: #fce4ec; }
    .toggle { position: relative; display: inline-block; width: 40px; height: 22px; }
    .toggle input { opacity: 0; width: 0; height: 0; }
    .slider { position: absolute; cursor: pointer; inset: 0; background: #ccc; border-radius: 22px; transition: 0.3s; }
    .slider::before { content: ""; position: absolute; height: 16px; width: 16px; left: 3px; bottom: 3px; background: white; border-radius: 50%; transition: 0.3s; }
    .toggle input:checked + .slider { background: #4CAF50; }
    .toggle input:checked + .slider::before { transform: translateX(18px); }
    .test-result { margin-top: 0.5rem; padding: 0.4rem 0.8rem; border-radius: 4px; font-size: 0.85rem; }
    .test-result.success { background: #e8f5e9; color: #2e7d32; }
    .test-result.error { background: #fce4ec; color: #c62828; }
    .deliveries-panel { margin-top: 1rem; border-top: 1px solid #eee; padding-top: 1rem; }
    .deliveries-table { width: 100%; border-collapse: collapse; font-size: 0.85rem; }
    .deliveries-table th, .deliveries-table td { padding: 0.5rem; text-align: left; }
    .deliveries-table th { color: #666; font-weight: 600; border-bottom: 1px solid #eee; }
    .status-dot { display: inline-block; width: 8px; height: 8px; border-radius: 50%; margin-right: 4px; }
    .status-dot.success { background: #4CAF50; }
    .status-dot.fail { background: #f44336; }
    .empty-state { text-align: center; padding: 3rem; color: #888; }
    .form-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
    .form-panel { background: white; padding: 2rem; border-radius: 12px; width: 500px; max-height: 90vh; overflow-y: auto; }
    .form-group { margin-bottom: 1rem; }
    .form-group label { display: block; font-weight: 500; margin-bottom: 0.4rem; color: #333; }
    .form-group input, .form-group select { width: 100%; padding: 0.6rem; border: 1px solid #ddd; border-radius: 6px; font-size: 0.9rem; box-sizing: border-box; }
    .event-checkboxes { display: grid; grid-template-columns: 1fr 1fr; gap: 0.5rem; }
    .checkbox-label { display: flex; align-items: center; gap: 0.5rem; font-size: 0.85rem; cursor: pointer; }
    .form-actions { display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 1.5rem; }
    .btn-cancel { padding: 0.6rem 1.2rem; background: #f5f5f5; border: 1px solid #ddd; border-radius: 6px; cursor: pointer; }
  `]
})
export class WebhooksComponent implements OnInit {
  webhooks: WebhookConfig[] = [];
  deliveries: DeliveryLog[] = [];
  expandedWebhook: string | null = null;
  testResult: Record<string, string> = {};
  showForm = false;
  editingId: string | null = null;
  formName = '';
  formUrl = '';
  formSecret = '';
  formChannel: 'WEBHOOK' | 'SLACK' | 'EMAIL' = 'WEBHOOK';
  formEvents: string[] = [];
  eventTypes = EVENT_TYPES;
  orgId = localStorage.getItem('orgId') || '';

  constructor(private webhookService: WebhookService) {}

  ngOnInit(): void {
    this.loadWebhooks();
  }

  loadWebhooks(): void {
    if (!this.orgId) return;
    this.webhookService.list(this.orgId).subscribe(w => this.webhooks = w);
  }

  resetForm(): void {
    this.editingId = null;
    this.formName = '';
    this.formUrl = '';
    this.formSecret = '';
    this.formChannel = 'WEBHOOK';
    this.formEvents = [];
  }

  editWebhook(wh: WebhookConfig): void {
    this.editingId = wh.id;
    this.formName = wh.name;
    this.formUrl = wh.url;
    this.formSecret = '';
    this.formChannel = wh.channel;
    this.formEvents = [...wh.events];
    this.showForm = true;
  }

  saveWebhook(): void {
    if (this.editingId) {
      this.webhookService.update(this.orgId, this.editingId, {
        name: this.formName, url: this.formUrl, channel: this.formChannel, events: this.formEvents
      }).subscribe(() => { this.showForm = false; this.loadWebhooks(); });
    } else {
      const request: CreateWebhookRequest = {
        name: this.formName, url: this.formUrl, channel: this.formChannel,
        events: this.formEvents, secretToken: this.formSecret || undefined
      };
      this.webhookService.create(this.orgId, request).subscribe(() => { this.showForm = false; this.loadWebhooks(); });
    }
  }

  deleteWebhook(wh: WebhookConfig): void {
    if (confirm(`Delete webhook "${wh.name}"?`)) {
      this.webhookService.delete(this.orgId, wh.id).subscribe(() => this.loadWebhooks());
    }
  }

  toggleActive(wh: WebhookConfig): void {
    this.webhookService.update(this.orgId, wh.id, { active: !wh.active }).subscribe(() => this.loadWebhooks());
  }

  testWebhook(wh: WebhookConfig): void {
    this.testResult[wh.id] = '';
    this.webhookService.test(this.orgId, wh.id).subscribe({
      next: () => { this.testResult[wh.id] = 'success'; setTimeout(() => delete this.testResult[wh.id], 3000); },
      error: () => { this.testResult[wh.id] = 'error'; setTimeout(() => delete this.testResult[wh.id], 3000); }
    });
  }

  toggleDeliveries(wh: WebhookConfig): void {
    if (this.expandedWebhook === wh.id) {
      this.expandedWebhook = null;
      return;
    }
    this.expandedWebhook = wh.id;
    this.webhookService.getDeliveries(this.orgId, wh.id).subscribe(page => this.deliveries = page.content);
  }

  toggleEvent(evt: string): void {
    const idx = this.formEvents.indexOf(evt);
    if (idx >= 0) this.formEvents.splice(idx, 1);
    else this.formEvents.push(evt);
  }
}
