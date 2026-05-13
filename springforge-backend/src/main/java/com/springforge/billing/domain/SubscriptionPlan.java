package com.springforge.billing.domain;

public enum SubscriptionPlan {
    FREE(0, 5, 10),
    PRO(2900, 50, 100),
    ENTERPRISE(9900, -1, -1);

    private final int priceInCents;
    private final int maxGenerationsPerMonth;
    private final int maxBlueprintsPerMonth;

    SubscriptionPlan(int priceInCents, int maxGenerationsPerMonth, int maxBlueprintsPerMonth) {
        this.priceInCents = priceInCents;
        this.maxGenerationsPerMonth = maxGenerationsPerMonth;
        this.maxBlueprintsPerMonth = maxBlueprintsPerMonth;
    }

    public int getPriceInCents() { return priceInCents; }
    public int getMaxGenerationsPerMonth() { return maxGenerationsPerMonth; }
    public int getMaxBlueprintsPerMonth() { return maxBlueprintsPerMonth; }
    public boolean isUnlimited() { return maxGenerationsPerMonth == -1; }
}
