export interface WebhookConfig {
  id: string;
  name: string;
  url: string;
  channel: 'WEBHOOK' | 'SLACK' | 'EMAIL';
  events: string[];
  active: boolean;
}

export interface CreateWebhookRequest {
  name: string;
  url: string;
  secretToken?: string;
  channel: 'WEBHOOK' | 'SLACK' | 'EMAIL';
  events: string[];
}

export interface UpdateWebhookRequest {
  name?: string;
  url?: string;
  channel?: 'WEBHOOK' | 'SLACK' | 'EMAIL';
  events?: string[];
  active?: boolean;
}

export interface DeliveryLog {
  id: string;
  eventType: string;
  httpStatus: number;
  success: boolean;
  attemptCount: number;
  createdAt: string;
}

export interface DeliveryPage {
  content: DeliveryLog[];
  totalElements: number;
  totalPages: number;
  number: number;
}

export const EVENT_TYPES = [
  'GENERATION_COMPLETED',
  'GENERATION_FAILED',
  'QUOTA_WARNING',
  'QUOTA_EXCEEDED',
  'SUBSCRIPTION_CHANGED',
  'MEMBER_JOINED'
];
