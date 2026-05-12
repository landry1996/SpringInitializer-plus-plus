package com.springforge.tenant;

public enum SubscriptionPlan {
    FREE(5, 2, 10),
    PRO(50, 10, 100),
    ENTERPRISE(-1, -1, -1);

    private final int maxProjects;
    private final int maxUsers;
    private final int maxGenerationsPerDay;

    SubscriptionPlan(int maxProjects, int maxUsers, int maxGenerationsPerDay) {
        this.maxProjects = maxProjects;
        this.maxUsers = maxUsers;
        this.maxGenerationsPerDay = maxGenerationsPerDay;
    }

    public int getMaxProjects() { return maxProjects; }
    public int getMaxUsers() { return maxUsers; }
    public int getMaxGenerationsPerDay() { return maxGenerationsPerDay; }

    public boolean isUnlimited() { return this == ENTERPRISE; }
}
