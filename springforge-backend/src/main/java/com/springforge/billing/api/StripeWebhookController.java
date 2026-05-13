package com.springforge.billing.api;

import com.springforge.billing.application.BillingService;
import com.springforge.billing.domain.SubscriptionPlan;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/webhooks")
public class StripeWebhookController {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookController.class);

    private final BillingService billingService;

    @Value("${app.billing.stripe.webhook-secret:}")
    private String webhookSecret;

    public StripeWebhookController(BillingService billingService) {
        this.billingService = billingService;
    }

    @PostMapping("/stripe")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            Event event;
            if (webhookSecret != null && !webhookSecret.isBlank()) {
                event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            } else {
                event = Event.GSON.fromJson(payload, Event.class);
            }

            StripeObject stripeObject = event.getDataObjectDeserializer()
                    .getObject().orElse(null);

            if (stripeObject == null) {
                log.warn("Failed to deserialize Stripe event: {}", event.getType());
                return ResponseEntity.ok("ignored");
            }

            switch (event.getType()) {
                case "checkout.session.completed" -> handleCheckoutSession((Session) stripeObject);
                case "invoice.paid" -> handleInvoicePaid(stripeObject);
                case "invoice.payment_failed" -> handleInvoiceFailed(stripeObject);
                case "customer.subscription.deleted" -> handleSubscriptionDeleted(stripeObject);
                default -> log.debug("Unhandled event type: {}", event.getType());
            }

            return ResponseEntity.ok("ok");
        } catch (Exception e) {
            log.error("Webhook processing failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook error");
        }
    }

    private void handleCheckoutSession(Session session) {
        String userId = session.getMetadata().get("userId");
        String planName = session.getMetadata().get("plan");
        if (userId != null && planName != null) {
            billingService.handleCheckoutCompleted(
                    session.getCustomer(),
                    session.getSubscription(),
                    UUID.fromString(userId),
                    SubscriptionPlan.valueOf(planName)
            );
        }
    }

    private void handleInvoicePaid(StripeObject obj) {
        com.stripe.model.Invoice invoice = (com.stripe.model.Invoice) obj;
        billingService.handleInvoicePaid(
                invoice.getId(),
                invoice.getCustomer(),
                invoice.getAmountPaid().intValue(),
                invoice.getCurrency()
        );
    }

    private void handleInvoiceFailed(StripeObject obj) {
        com.stripe.model.Invoice invoice = (com.stripe.model.Invoice) obj;
        billingService.handleInvoicePaymentFailed(invoice.getCustomer());
    }

    private void handleSubscriptionDeleted(StripeObject obj) {
        com.stripe.model.Subscription subscription = (com.stripe.model.Subscription) obj;
        billingService.handleSubscriptionDeleted(subscription.getId());
    }
}
