package com.springforge.admin;

import java.util.List;
import java.util.Map;

public record DashboardStats(
    long totalProjects,
    long totalUsers,
    long activeUsers,
    long totalBlueprints,
    long projectsToday,
    long projectsThisWeek,
    long projectsThisMonth,
    Map<String, Long> projectsByArchitecture,
    Map<String, Long> projectsByJavaVersion,
    Map<String, Long> topDependencies,
    List<DailyStats> dailyStats
) {}
