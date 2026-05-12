export interface AdminUser {
  id: string;
  username: string;
  email: string;
  role: 'ADMIN' | 'USER' | 'VIEWER';
  active: boolean;
  lastLogin: string | null;
  createdAt: string;
}

export interface AuditLog {
  id: string;
  userId: string;
  action: string;
  details: string;
  ipAddress: string;
  timestamp: string;
}

export interface DailyStats {
  date: string;
  count: number;
}

export interface DashboardStats {
  totalProjects: number;
  totalUsers: number;
  activeUsers: number;
  totalBlueprints: number;
  projectsToday: number;
  projectsThisWeek: number;
  projectsThisMonth: number;
  projectsByArchitecture: Record<string, number>;
  projectsByJavaVersion: Record<string, number>;
  topDependencies: Record<string, number>;
  dailyStats: DailyStats[];
}
