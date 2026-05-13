package com.springforge.billing.application;

import com.springforge.billing.domain.*;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BillingService {

    private static final Logger log = LoggerFactory.getLogger(BillingService.class);

    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;

    @Value("${app.billing.stripe.secret-key:}")
    private String stripeSecretKey;

    @Value("${app.billing.stripe.pro-price-id:}")
    private String proPriceId;

    @Value("${app.billing.stripe.enterprise-price-id:}")
    private String enterprisePriceId;

    @Value("${app.billing.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    public BillingService(SubscriptionRepository subscriptionRepository,
                         InvoiceRepository invoiceRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @PostConstruct
    public void init() {
        if (stripeSecretKey != null && !stripeSecretKey.isBlank()) {
            Stripe.apiKey = stripeSecretKey;
        }
    }

    @Transactional(readOnly = true)
    public Subscription getOrCreateSubscription(UUID userId) {
        return subscriptionRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Subscription sub = Subscription.createProTrial(userId);
                    return subscriptionRepository.save(sub);
                });
    }

    @Transactional
    public String createCheckoutSession(UUID userId, SubscriptionPlan plan) {
        try {
            Subscription subscription = getOrCreateSubscription(userId);
            String priceId = plan == SubscriptionPlan.ENTERPRISE ? enterprisePriceId : proPriceId;

            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setSuccessUrl(frontendUrl + "/billing/success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(frontendUrl + "/billing/cancel")
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setPrice(priceId)
                            .setQuantity(1L)
                            .build())
                    .putMetadata("userId", userId.toString())
                    .putMetadata("plan", plan.name());

            if (subscription.getStripeCustomerId() != null) {
                paramsBuilder.setCustomer(subscription.getStripeCustomerId());
            } else {
                paramsBuilder.setCustomerCreation(SessionCreateParams.CustomerCreation.ALWAYS);
            }

            Session session = Session.create(paramsBuilder.build());
            return session.getUrl();
        } catch (Exception e) {
            log.error("Failed to create checkout session", e);
            throw new RuntimeException("Failed to create checkout session", e);
        }
    }

    @Transactional
    public String createPortalSession(UUID userId) {
        try {
            Subscription subscription = getOrCreateSubscription(userId);
            if (subscription.getStripeCustomerId() == null) {
                throw new IllegalStateException("No Stripe customer found for user");
            }

            com.stripe.param.billingportal.SessionCreateParams params =
                    com.stripe.param.billingportal.SessionCreateParams.builder()
                            .setCustomer(subscription.getStripeCustomerId())
                            .setReturnUrl(frontendUrl + "/billing")
                            .build();

            com.stripe.model.billingportal.Session session =
                    com.stripe.model.billingportal.Session.create(params);
            return session.getUrl();
        } catch (Exception e) {
            log.error("Failed to create portal session", e);
            throw new RuntimeException("Failed to create portal session", e);
        }
    }

    @Transactional
    public void handleCheckoutCompleted(String stripeCustomerId, String stripeSubscriptionId,
                                        UUID userId, SubscriptionPlan plan) {
        Subscription subscription = getOrCreateSubscription(userId);
        subscription.setStripeCustomerId(stripeCustomerId);
        subscription.changePlan(plan);
        subscription.activate(stripeSubscriptionId, LocalDateTime.now(), LocalDateTime.now().plusMonths(1));
        subscription.convertFromTrial();
        subscriptionRepository.save(subscription);
        log.info("Subscription activated for user {}: {}", userId, plan);
    }

    @Transactional
    public void handleInvoicePaid(String stripeInvoiceId, String stripeCustomerId,
                                  int amountCents, String currency) {
        subscriptionRepository.findByStripeCustomerId(stripeCustomerId).ifPresent(subscription -> {
            Invoice invoice = new Invoice(subscription.getUserId(), stripeInvoiceId, amountCents, currency);
            invoice.markPaid(LocalDateTime.now());
            invoiceRepository.save(invoice);
        });
    }

    @Transactional
    public void handleInvoicePaymentFailed(String stripeCustomerId) {
        subscriptionRepository.findByStripeCustomerId(stripeCustomerId).ifPresent(subscription -> {
            subscription.markPastDue();
            subscriptionRepository.save(subscription);
            log.warn("Payment failed for customer {}", stripeCustomerId);
        });
    }

    @Transactional
    public void handleSubscriptionDeleted(String stripeSubscriptionId) {
        subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId).ifPresent(subscription -> {
            subscription.cancel();
            subscription.changePlan(SubscriptionPlan.FREE);
            subscriptionRepository.save(subscription);
            log.info("Subscription canceled: {}", stripeSubscriptionId);
        });
    }

    @Transactional(readOnly = true)
    public Page<Invoice> getInvoices(UUID userId, Pageable pageable) {
        return invoiceRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void expireTrials() {
        List<Subscription> expired = subscriptionRepository.findByTrialTrueAndTrialEndsAtBefore(LocalDateTime.now());
        for (Subscription sub : expired) {
            sub.expireTrial();
            subscriptionRepository.save(sub);
            log.info("Trial expired for user {}, downgraded to FREE", sub.getUserId());
        }
    }
}
