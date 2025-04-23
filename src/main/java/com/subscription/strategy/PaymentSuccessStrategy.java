package com.subscription.strategy;

import com.subscription.model.event.PaymentStatusUpdated;
import com.subscription.model.enums.PaymentEventType;
import com.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentSuccessStrategy implements PaymentEventStrategy {

    private final SubscriptionService subscriptionService;

    @Override
    public PaymentEventType getEventType() {
        return PaymentEventType.PAYMENT_SUCCESS;
    }

    @Override
    public void handle(PaymentStatusUpdated event) {
        subscriptionService.handlePaymentSuccess(event);
    }
} 