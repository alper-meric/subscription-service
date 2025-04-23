package com.subscription.model.request;

import lombok.Data;

import java.util.UUID;

@Data
public class SubscriptionRequest {
    private UUID userId;
    private Long planId;
    private Double price;
    private Long paymentMethodId;
} 