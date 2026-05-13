import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BillingService } from './billing.service';
import { Subscription, Invoice } from './billing.model';

@Component({
  selector: 'app-billing',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="billing-page">
      <h2>Billing & Subscription</h2>

      <div class="plans-section">
        <div class="plan-card" [class.active]="subscription?.plan === 'FREE'">
          <h3>Free</h3>
          <div class="price">0<span class="currency">&euro;</span><span class="period">/month</span></div>
          <ul>
            <li>5 generations/month</li>
            <li>2 members</li>
            <li>Basic recommendations</li>
            <li>Community support</li>
          </ul>
          <button *ngIf="subscription?.plan !== 'FREE'" class="btn-plan" disabled>Current minimum</button>
          <span *ngIf="subscription?.plan === 'FREE'" class="current-badge">Current Plan</span>
        </div>

        <div class="plan-card featured" [class.active]="subscription?.plan === 'PRO'">
          <div class="popular-tag">Popular</div>
          <h3>Pro</h3>
          <div class="price">29<span class="currency">&euro;</span><span class="period">/month</span></div>
          <ul>
            <li>50 generations/month</li>
            <li>10 members</li>
            <li>Full AI recommendations</li>
            <li>Email support</li>
            <li>LLM assistant</li>
          </ul>
          <button *ngIf="subscription?.plan !== 'PRO'" class="btn-plan btn-primary" (click)="checkout('PRO')">
            {{ subscription?.plan === 'ENTERPRISE' ? 'Downgrade' : 'Upgrade' }} to Pro
          </button>
          <span *ngIf="subscription?.plan === 'PRO'" class="current-badge">Current Plan</span>
        </div>

        <div class="plan-card" [class.active]="subscription?.plan === 'ENTERPRISE'">
          <h3>Enterprise</h3>
          <div class="price">99<span class="currency">&euro;</span><span class="period">/month</span></div>
          <ul>
            <li>Unlimited generations</li>
            <li>Unlimited members</li>
            <li>Full AI + priority</li>
            <li>Dedicated support</li>
            <li>Custom blueprints</li>
          </ul>
          <button *ngIf="subscription?.plan !== 'ENTERPRISE'" class="btn-plan btn-primary" (click)="checkout('ENTERPRISE')">
            Upgrade to Enterprise
          </button>
          <span *ngIf="subscription?.plan === 'ENTERPRISE'" class="current-badge">Current Plan</span>
        </div>
      </div>

      <div class="subscription-details" *ngIf="subscription && subscription.plan !== 'FREE'">
        <h4>Subscription Details</h4>
        <div class="detail-row">
          <span>Status</span>
          <span class="status-badge" [class]="subscription.status.toLowerCase()">{{ subscription.status }}</span>
        </div>
        <div class="detail-row" *ngIf="subscription.currentPeriodEnd">
          <span>Next billing date</span>
          <span>{{ subscription.currentPeriodEnd | date:'mediumDate' }}</span>
        </div>
        <div class="detail-row" *ngIf="subscription.trialEndsAt">
          <span>Trial ends</span>
          <span>{{ subscription.trialEndsAt | date:'mediumDate' }}</span>
        </div>
        <button class="btn-manage" (click)="openPortal()">Manage Subscription</button>
      </div>

      <div class="invoices-section" *ngIf="invoices.length > 0">
        <h4>Invoice History</h4>
        <table class="invoices-table">
          <thead>
            <tr>
              <th>Date</th>
              <th>Amount</th>
              <th>Status</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let invoice of invoices">
              <td>{{ invoice.createdAt | date:'mediumDate' }}</td>
              <td>{{ (invoice.amountCents / 100).toFixed(2) }} {{ invoice.currency.toUpperCase() }}</td>
              <td><span class="invoice-status" [class]="invoice.status.toLowerCase()">{{ invoice.status }}</span></td>
              <td>
                <a *ngIf="invoice.invoiceUrl" [href]="invoice.invoiceUrl" target="_blank" class="link">View</a>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .billing-page { padding: 2rem; max-width: 1000px; margin: 0 auto; }
    .plans-section { display: grid; grid-template-columns: repeat(3, 1fr); gap: 1.5rem; margin: 2rem 0; }
    .plan-card { border: 2px solid #e0e0e0; border-radius: 12px; padding: 2rem; text-align: center; position: relative; transition: transform 0.2s, box-shadow 0.2s; }
    .plan-card:hover { transform: translateY(-4px); box-shadow: 0 8px 25px rgba(0,0,0,0.1); }
    .plan-card.active { border-color: #1976d2; background: #f3f8ff; }
    .plan-card.featured { border-color: #1976d2; }
    .popular-tag { position: absolute; top: -12px; left: 50%; transform: translateX(-50%); background: #1976d2; color: white; padding: 4px 16px; border-radius: 12px; font-size: 0.75rem; font-weight: bold; }
    .price { font-size: 3rem; font-weight: bold; color: #333; margin: 1rem 0; }
    .price .currency { font-size: 1.5rem; vertical-align: super; }
    .price .period { font-size: 0.9rem; color: #666; font-weight: normal; }
    ul { list-style: none; padding: 0; margin: 1.5rem 0; text-align: left; }
    li { padding: 0.5rem 0; border-bottom: 1px solid #f0f0f0; color: #555; }
    li::before { content: "\\2713"; color: #4CAF50; margin-right: 0.5rem; font-weight: bold; }
    .btn-plan { width: 100%; padding: 0.75rem; border: 2px solid #1976d2; border-radius: 8px; cursor: pointer; font-weight: bold; background: white; color: #1976d2; font-size: 0.9rem; }
    .btn-plan.btn-primary { background: #1976d2; color: white; }
    .btn-plan:hover { opacity: 0.9; }
    .btn-plan:disabled { opacity: 0.5; cursor: not-allowed; }
    .current-badge { display: inline-block; padding: 0.5rem 1rem; background: #e8f5e9; color: #2e7d32; border-radius: 8px; font-weight: bold; }
    .subscription-details { background: #f8f9fa; padding: 1.5rem; border-radius: 8px; margin: 2rem 0; }
    .detail-row { display: flex; justify-content: space-between; padding: 0.5rem 0; border-bottom: 1px solid #eee; }
    .status-badge { padding: 4px 12px; border-radius: 4px; font-size: 0.8rem; font-weight: bold; }
    .status-badge.active { background: #e8f5e9; color: #2e7d32; }
    .status-badge.past_due { background: #fff3e0; color: #e65100; }
    .status-badge.canceled { background: #fce4ec; color: #c62828; }
    .btn-manage { margin-top: 1rem; padding: 0.6rem 1.5rem; background: #424242; color: white; border: none; border-radius: 6px; cursor: pointer; }
    .invoices-section { margin-top: 2rem; }
    .invoices-table { width: 100%; border-collapse: collapse; }
    .invoices-table th, .invoices-table td { padding: 0.75rem; text-align: left; border-bottom: 1px solid #eee; }
    .invoices-table th { font-weight: 600; color: #666; font-size: 0.85rem; text-transform: uppercase; }
    .invoice-status { padding: 3px 8px; border-radius: 4px; font-size: 0.8rem; }
    .invoice-status.paid { background: #e8f5e9; color: #2e7d32; }
    .invoice-status.pending { background: #fff3e0; color: #e65100; }
    .invoice-status.failed { background: #fce4ec; color: #c62828; }
    .link { color: #1976d2; text-decoration: none; }
    .link:hover { text-decoration: underline; }
  `]
})
export class BillingComponent implements OnInit {
  subscription: Subscription | null = null;
  invoices: Invoice[] = [];

  constructor(private billingService: BillingService) {}

  ngOnInit(): void {
    this.billingService.getSubscription().subscribe(s => this.subscription = s);
    this.billingService.getInvoices().subscribe(i => this.invoices = i);
  }

  checkout(plan: string): void {
    this.billingService.createCheckout(plan).subscribe(res => {
      window.location.href = res.sessionUrl;
    });
  }

  openPortal(): void {
    this.billingService.createPortalSession().subscribe(res => {
      window.location.href = res.portalUrl;
    });
  }
}
