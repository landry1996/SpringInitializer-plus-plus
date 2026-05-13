package com.springforge.billing.domain;

import com.springforge.shared.domain.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "subscriptions", schema = "springforge")
public class Subscription extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionPlan plan;

    @Column(name = "stripe_customer_id")
    private String stripeCustomerId;

    @Column(name = "stripe_subscription_id")
    private String stripeSubscriptionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    @Column(name = "current_period_start")
    private LocalDateTime currentPeriodStart;

    @Column(name = "current_period_end")
    private LocalDateTime currentPeriodEnd;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Column(name = "trial")
    private boolean trial;

    @Column(name = "trial_ends_at")
    private LocalDateTime trialEndsAt;

    protected Subscription() {}

    public Subscription(UUID userId, SubscriptionPlan plan) {
        this.userId = userId;
        this.plan = plan;
        this.status = SubscriptionStatus.ACTIVE;
        this.currentPeriodStart = LocalDateTime.now();
        this.currentPeriodEnd = LocalDateTime.now().plusMonths(1);
        this.trial = false;
    }

    public static Subscription createProTrial(UUID userId) {
        Subscription sub = new Subscription(userId, SubscriptionPlan.PRO);
        sub.trial = true;
        sub.trialEndsAt = LocalDateTime.now().plusDays(14);
        return sub;
    }

    public void expireTrial() {
        this.trial = false;
        this.trialEndsAt = null;
        this.plan = SubscriptionPlan.FREE;
    }

    public void convertFromTrial() {
        this.trial = false;
        this.trialEndsAt = null;
    }

    public void activate(String stripeSubscriptionId, LocalDateTime periodStart, LocalDateTime periodEnd) {
        this.stripeSubscriptionId = stripeSubscriptionId;
        this.status = SubscriptionStatus.ACTIVE;
        this.currentPeriodStart = periodStart;
        this.currentPeriodEnd = periodEnd;
    }

    public void cancel() {
        this.status = SubscriptionStatus.CANCELED;
        this.canceledAt = LocalDateTime.now();
    }

    public void changePlan(SubscriptionPlan newPlan) {
        this.plan = newPlan;
    }

    public void markPastDue() {
        this.status = SubscriptionStatus.PAST_DUE;
    }

    public UUID getUserId() { return userId; }
    public SubscriptionPlan getPlan() { return plan; }
    public String getStripeCustomerId() { return stripeCustomerId; }
    public void setStripeCustomerId(String stripeCustomerId) { this.stripeCustomerId = stripeCustomerId; }
    public String getStripeSubscriptionId() { return stripeSubscriptionId; }
    public SubscriptionStatus getStatus() { return status; }
    public LocalDateTime getCurrentPeriodStart() { return currentPeriodStart; }
    public LocalDateTime getCurrentPeriodEnd() { return currentPeriodEnd; }
    public LocalDateTime getCanceledAt() { return canceledAt; }
    public boolean isTrial() { return trial; }
    public LocalDateTime getTrialEndsAt() { return trialEndsAt; }
}
