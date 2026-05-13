import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { WebhookConfig, CreateWebhookRequest, UpdateWebhookRequest, DeliveryPage } from './webhook.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class WebhookService {
  private baseUrl = `${environment.apiUrl}/organizations`;

  constructor(private http: HttpClient) {}

  list(orgId: string): Observable<WebhookConfig[]> {
    return this.http.get<WebhookConfig[]>(`${this.baseUrl}/${orgId}/webhooks`);
  }

  create(orgId: string, request: CreateWebhookRequest): Observable<WebhookConfig> {
    return this.http.post<WebhookConfig>(`${this.baseUrl}/${orgId}/webhooks`, request);
  }

  update(orgId: string, webhookId: string, request: UpdateWebhookRequest): Observable<WebhookConfig> {
    return this.http.put<WebhookConfig>(`${this.baseUrl}/${orgId}/webhooks/${webhookId}`, request);
  }

  delete(orgId: string, webhookId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${orgId}/webhooks/${webhookId}`);
  }

  test(orgId: string, webhookId: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${orgId}/webhooks/${webhookId}/test`, {});
  }

  getDeliveries(orgId: string, webhookId: string, page: number = 0, size: number = 20): Observable<DeliveryPage> {
    return this.http.get<DeliveryPage>(
      `${this.baseUrl}/${orgId}/webhooks/${webhookId}/deliveries?page=${page}&size=${size}`
    );
  }
}
