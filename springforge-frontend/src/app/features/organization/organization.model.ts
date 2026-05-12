export interface Organization {
  id: string;
  name: string;
  slug: string;
  ownerEmail: string;
  plan: 'FREE' | 'PRO' | 'ENTERPRISE';
  maxProjects: number;
  maxUsers: number;
  currentProjectCount: number;
  currentUserCount: number;
  active: boolean;
  trialEndsAt: string | null;
  createdAt: string;
}

export interface OrganizationMember {
  id: string;
  organizationId: string;
  userId: string;
  email: string;
  role: 'OWNER' | 'ADMIN' | 'MEMBER' | 'VIEWER';
  joinedAt: string;
}

export interface ApiKey {
  id: string;
  organizationId: string;
  name: string;
  keyPrefix: string;
  scopes: string[];
  active: boolean;
  lastUsedAt: string | null;
  expiresAt: string;
  createdAt: string;
}

export interface UsageReport {
  organizationId: string;
  plan: string;
  projectsUsed: number;
  projectsLimit: number;
  usersUsed: number;
  usersLimit: number;
  generationsToday: number;
  generationsLimit: number;
  storageUsedMb: number;
  storageLimitMb: number;
}
