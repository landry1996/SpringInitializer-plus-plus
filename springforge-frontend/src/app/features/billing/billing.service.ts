import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Subscription, Invoice, CheckoutResponse, PortalResponse } from './billing.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class BillingService {
  private baseUrl = `${environment.apiUrl}/billing`;

  constructor(private http: HttpClient) {}

  getSubscription(): Observable<Subscription> {
    return this.http.get<Subscription>(`${this.baseUrl}/subscription`);
  }

  createCheckout(plan: string): Observable<CheckoutResponse> {
    return this.http.post<CheckoutResponse>(`${this.baseUrl}/checkout`, { plan });
  }

  createPortalSession(): Observable<PortalResponse> {
    return this.http.post<PortalResponse>(`${this.baseUrl}/portal`, {});
  }

  getInvoices(): Observable<Invoice[]> {
    return this.http.get<Invoice[]>(`${this.baseUrl}/invoices`);
  }
}
