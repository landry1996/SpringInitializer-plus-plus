export interface Subscription {
  id: string;
  organizationId: string;
  plan: 'FREE' | 'PRO' | 'ENTERPRISE';
  status: 'ACTIVE' | 'PAST_DUE' | 'CANCELED' | 'INCOMPLETE';
  stripeCustomerId: string | null;
  stripeSubscriptionId: string | null;
  currentPeriodEnd: string | null;
  trial: boolean;
  trialEndsAt: string | null;
  createdAt: string;
}

export interface Invoice {
  id: string;
  stripeInvoiceId: string;
  amountCents: number;
  currency: string;
  status: 'PENDING' | 'PAID' | 'FAILED' | 'VOID';
  paidAt: string | null;
  invoiceUrl: string | null;
  createdAt: string;
}

export interface CheckoutResponse {
  sessionUrl: string;
}

export interface PortalResponse {
  portalUrl: string;
}
