package com.subscription.strategy;

import com.subscription.model.event.PaymentStatusUpdated;
import com.subscription.model.enums.PaymentEventType;

public interface PaymentEventStrategy {
    PaymentEventType getEventType();
    void handle(PaymentStatusUpdated event);
} 