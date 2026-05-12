package com.springforge.tenant;

public record UsageReport(
    String organizationId,
    SubscriptionPlan plan,
    int projectsUsed,
    int projectsLimit,
    int usersUsed,
    int usersLimit,
    int generationsToday,
    int generationsLimit,
    double storageUsedMb,
    double storageLimitMb
) {}
