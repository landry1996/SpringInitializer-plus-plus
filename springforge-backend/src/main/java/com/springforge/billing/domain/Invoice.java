package com.springforge.billing.domain;

import com.springforge.shared.domain.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invoices", schema = "springforge")
public class Invoice extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "stripe_invoice_id", unique = true)
    private String stripeInvoiceId;

    @Column(name = "amount_cents", nullable = false)
    private int amountCents;

    @Column(nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "invoice_url")
    private String invoiceUrl;

    protected Invoice() {}

    public Invoice(UUID userId, String stripeInvoiceId, int amountCents, String currency) {
        this.userId = userId;
        this.stripeInvoiceId = stripeInvoiceId;
        this.amountCents = amountCents;
        this.currency = currency;
        this.status = InvoiceStatus.PENDING;
    }

    public void markPaid(LocalDateTime paidAt) {
        this.status = InvoiceStatus.PAID;
        this.paidAt = paidAt;
    }

    public void markFailed() {
        this.status = InvoiceStatus.FAILED;
    }

    public UUID getUserId() { return userId; }
    public String getStripeInvoiceId() { return stripeInvoiceId; }
    public int getAmountCents() { return amountCents; }
    public String getCurrency() { return currency; }
    public InvoiceStatus getStatus() { return status; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public String getInvoiceUrl() { return invoiceUrl; }
    public void setInvoiceUrl(String invoiceUrl) { this.invoiceUrl = invoiceUrl; }
}
