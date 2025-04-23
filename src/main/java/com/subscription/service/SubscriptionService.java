package com.subscription.service;

import com.subscription.model.request.SubscriptionRequest;
import com.subscription.model.response.SubscriptionResponse;
import com.subscription.model.event.PaymentStatusUpdated;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface SubscriptionService {

    @Transactional
    SubscriptionResponse createSubscription(SubscriptionRequest request);

    @Transactional
    SubscriptionResponse cancelSubscription(UUID subscriptionId);

    SubscriptionResponse getSubscription(UUID subscriptionId);

    List<SubscriptionResponse> getUserSubscriptions(UUID userId);

    @Transactional
    void processExpiredSubscriptions();

    @Transactional
    void processRenewalSubscriptions();

    @Transactional
    void handlePaymentSuccess(PaymentStatusUpdated event);

    @Transactional
    void handlePaymentFailed(PaymentStatusUpdated event);
}
