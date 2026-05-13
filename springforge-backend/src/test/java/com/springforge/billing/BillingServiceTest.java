package com.springforge.billing;

import com.springforge.billing.application.BillingService;
import com.springforge.billing.domain.*;
import com.springforge.billing.domain.InvoiceRepository;
import com.springforge.billing.domain.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    private BillingService billingService;

    @BeforeEach
    void setUp() {
        billingService = new BillingService(subscriptionRepository, invoiceRepository);
    }

    @Test
    void getOrCreateSubscription_shouldReturnExisting() {
        UUID userId = UUID.randomUUID();
        Subscription existing = new Subscription(userId, SubscriptionPlan.PRO);
        when(subscriptionRepository.findByUserId(userId)).thenReturn(Optional.of(existing));

        Subscription result = billingService.getOrCreateSubscription(userId);

        assertThat(result.getPlan()).isEqualTo(SubscriptionPlan.PRO);
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void getOrCreateSubscription_shouldCreateProTrialWhenNotExists() {
        UUID userId = UUID.randomUUID();
        when(subscriptionRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(subscriptionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Subscription result = billingService.getOrCreateSubscription(userId);

        assertThat(result.getPlan()).isEqualTo(SubscriptionPlan.PRO);
        assertThat(result.isTrial()).isTrue();
        assertThat(result.getTrialEndsAt()).isNotNull();
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void handleCheckoutCompleted_shouldActivateSubscription() {
        UUID userId = UUID.randomUUID();
        Subscription subscription = new Subscription(userId, SubscriptionPlan.FREE);
        when(subscriptionRepository.findByUserId(userId)).thenReturn(Optional.of(subscription));
        when(subscriptionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        billingService.handleCheckoutCompleted("cus_123", "sub_456", userId, SubscriptionPlan.PRO);

        ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository).save(captor.capture());
        assertThat(captor.getValue().getPlan()).isEqualTo(SubscriptionPlan.PRO);
        assertThat(captor.getValue().getStripeCustomerId()).isEqualTo("cus_123");
        assertThat(captor.getValue().getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    }

    @Test
    void handleInvoicePaid_shouldCreateInvoiceRecord() {
        UUID userId = UUID.randomUUID();
        Subscription subscription = new Subscription(userId, SubscriptionPlan.PRO);
        subscription.setStripeCustomerId("cus_123");
        when(subscriptionRepository.findByStripeCustomerId("cus_123")).thenReturn(Optional.of(subscription));
        when(invoiceRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        billingService.handleInvoicePaid("inv_789", "cus_123", 2900, "eur");

        ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(captor.capture());
        assertThat(captor.getValue().getAmountCents()).isEqualTo(2900);
        assertThat(captor.getValue().getStatus()).isEqualTo(InvoiceStatus.PAID);
    }

    @Test
    void handleInvoicePaymentFailed_shouldMarkPastDue() {
        Subscription subscription = new Subscription(UUID.randomUUID(), SubscriptionPlan.PRO);
        subscription.setStripeCustomerId("cus_123");
        when(subscriptionRepository.findByStripeCustomerId("cus_123")).thenReturn(Optional.of(subscription));
        when(subscriptionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        billingService.handleInvoicePaymentFailed("cus_123");

        ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(SubscriptionStatus.PAST_DUE);
    }

    @Test
    void handleSubscriptionDeleted_shouldCancelAndRevertToFree() {
        Subscription subscription = new Subscription(UUID.randomUUID(), SubscriptionPlan.PRO);
        when(subscriptionRepository.findByStripeSubscriptionId("sub_456")).thenReturn(Optional.of(subscription));
        when(subscriptionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        billingService.handleSubscriptionDeleted("sub_456");

        ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(SubscriptionStatus.CANCELED);
        assertThat(captor.getValue().getPlan()).isEqualTo(SubscriptionPlan.FREE);
    }
}
