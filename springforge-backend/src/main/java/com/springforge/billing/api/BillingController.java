package com.springforge.billing.api;

import com.springforge.billing.application.BillingService;
import com.springforge.billing.domain.Invoice;
import com.springforge.billing.domain.Subscription;
import com.springforge.billing.domain.SubscriptionPlan;
import com.springforge.shared.security.AuthenticatedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/billing")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @GetMapping("/subscription")
    public ResponseEntity<SubscriptionResponse> getSubscription(@AuthenticationPrincipal AuthenticatedUser user) {
        Subscription sub = billingService.getOrCreateSubscription(user.id());
        return ResponseEntity.ok(SubscriptionResponse.from(sub));
    }

    @PostMapping("/checkout")
    public ResponseEntity<Map<String, String>> createCheckout(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestBody CheckoutRequest request) {
        SubscriptionPlan plan = SubscriptionPlan.valueOf(request.plan().toUpperCase());
        String url = billingService.createCheckoutSession(user.id(), plan);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @PostMapping("/portal")
    public ResponseEntity<Map<String, String>> createPortal(@AuthenticationPrincipal AuthenticatedUser user) {
        String url = billingService.createPortalSession(user.id());
        return ResponseEntity.ok(Map.of("url", url));
    }

    @GetMapping("/invoices")
    public ResponseEntity<Page<InvoiceResponse>> getInvoices(
            @AuthenticationPrincipal AuthenticatedUser user, Pageable pageable) {
        Page<Invoice> invoices = billingService.getInvoices(user.id(), pageable);
        return ResponseEntity.ok(invoices.map(InvoiceResponse::from));
    }

    public record CheckoutRequest(String plan) {}

    public record SubscriptionResponse(String plan, String status, String currentPeriodEnd) {
        public static SubscriptionResponse from(Subscription sub) {
            return new SubscriptionResponse(
                    sub.getPlan().name(),
                    sub.getStatus().name(),
                    sub.getCurrentPeriodEnd() != null ? sub.getCurrentPeriodEnd().toString() : null
            );
        }
    }

    public record InvoiceResponse(String id, int amountCents, String currency, String status, String paidAt) {
        public static InvoiceResponse from(Invoice inv) {
            return new InvoiceResponse(
                    inv.getId().toString(),
                    inv.getAmountCents(),
                    inv.getCurrency(),
                    inv.getStatus().name(),
                    inv.getPaidAt() != null ? inv.getPaidAt().toString() : null
            );
        }
    }
}
