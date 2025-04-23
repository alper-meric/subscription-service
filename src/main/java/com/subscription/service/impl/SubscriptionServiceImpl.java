package com.subscription.service.impl;

import com.subscription.model.request.SubscriptionRequest;
import com.subscription.model.response.SubscriptionResponse;
import com.subscription.model.event.SubscriptionStatusUpdated;
import com.subscription.model.event.PaymentStatusUpdated;
import com.subscription.model.enums.SubscriptionEventType;
import com.subscription.exception.SubscriptionException;
import com.subscription.model.Subscription;
import com.subscription.model.enums.SubscriptionStatus;
import com.subscription.repository.SubscriptionRepository;
import com.subscription.service.SubscriptionService;
import com.subscription.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final OutboxService outboxService;

    @Value("${spring.kafka.producer.topics.subscription-status-updated.topic}")
    private String SUBSCRIPTION_STATUS_UPDATED_TOPIC;

    @Override
    @Transactional
    public SubscriptionResponse createSubscription(SubscriptionRequest request) {
        log.info("Creating subscription for user: {}", request.getUserId());

        Subscription subscription = Subscription.builder()
                .userId(request.getUserId())
                .planId(request.getPlanId())
                .status(SubscriptionStatus.PAYMENT_PENDING)
                .price(request.getPrice())
                .paymentMethodId(request.getPaymentMethodId())
                .build();

        subscription = subscriptionRepository.save(subscription);

        SubscriptionStatusUpdated event = SubscriptionStatusUpdated.createEvent(
                subscription.getId(),
                subscription.getUserId(),
                subscription.getPlanId(),
                subscription.getStatus(),
                SubscriptionEventType.CREATED,
                Map.of(
                        "price", subscription.getPrice(),
                        "paymentMethodId", subscription.getPaymentMethodId()
                )
        );

        outboxService.saveEvent(
                SubscriptionEventType.CREATED.name(),
                SUBSCRIPTION_STATUS_UPDATED_TOPIC,
                event,
                subscription.getId()
        );

        return mapToResponse(subscription);
    }

    @Override
    @Transactional
    public SubscriptionResponse cancelSubscription(UUID subscriptionId) {
        log.info("Cancelling subscription: {}", subscriptionId);

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new SubscriptionException("Subscription not found"));

        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new SubscriptionException("Only active subscriptions can be cancelled");
        }

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription = subscriptionRepository.save(subscription);

        SubscriptionStatusUpdated event = SubscriptionStatusUpdated.createEvent(
                subscription.getId(),
                subscription.getUserId(),
                subscription.getPlanId(),
                subscription.getStatus(),
                SubscriptionEventType.CANCELLED,
                Map.of(
                        "reason", "USER_REQUESTED",
                        "reasonDescription", "User requested cancellation"
                )
        );

        outboxService.saveEvent(
                SubscriptionEventType.CANCELLED.name(),
                SUBSCRIPTION_STATUS_UPDATED_TOPIC,
                event,
                subscription.getId()
        );

        return mapToResponse(subscription);
    }

    @Override
    public SubscriptionResponse getSubscription(UUID subscriptionId) {
        log.info("Getting subscription: {}", subscriptionId);

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new SubscriptionException("Subscription not found"));

        return mapToResponse(subscription);
    }

    @Override
    public List<SubscriptionResponse> getUserSubscriptions(UUID userId) {
        List<Subscription> subscriptions = subscriptionRepository.findByUserId(userId);
        return subscriptions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void processExpiredSubscriptions() {
        log.info("Processing expired subscriptions");

        LocalDateTime now = LocalDateTime.now();
        List<Subscription> expiredSubscriptions = subscriptionRepository
                .findByStatusAndEndDateBefore(SubscriptionStatus.ACTIVE, now);

        for (Subscription subscription : expiredSubscriptions) {
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscription = subscriptionRepository.save(subscription);

            SubscriptionStatusUpdated event = SubscriptionStatusUpdated.createEvent(
                    subscription.getId(),
                    subscription.getUserId(),
                    subscription.getPlanId(),
                    subscription.getStatus(),
                    SubscriptionEventType.EXPIRED,
                    Map.of(
                            "reason", "SUBSCRIPTION_EXPIRED",
                            "reasonDescription", "Subscription period ended"
                    )
            );

            outboxService.saveEvent(
                    SubscriptionEventType.EXPIRED.name(),
                    SUBSCRIPTION_STATUS_UPDATED_TOPIC,
                    event,
                    subscription.getId()
            );
        }
    }

    @Override
    @Transactional
    public void processRenewalSubscriptions() {
        log.info("Processing subscription renewals");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime renewalDate = now.plusDays(1); // Yarın yenilenecek abonelikler

        List<Subscription> renewalSubscriptions = subscriptionRepository
                .findByStatusAndEndDateBetween(SubscriptionStatus.ACTIVE, now, renewalDate);

        for (Subscription subscription : renewalSubscriptions) {
            SubscriptionStatusUpdated event = SubscriptionStatusUpdated.createEvent(
                    subscription.getId(),
                    subscription.getUserId(),
                    subscription.getPlanId(),
                    SubscriptionStatus.PAYMENT_PENDING,
                    SubscriptionEventType.RENEWAL,
                    Map.of(
                            "price", subscription.getPrice(),
                            "paymentMethodId", subscription.getPaymentMethodId()
                    )
            );

            outboxService.saveEvent(
                    SubscriptionEventType.RENEWAL.name(),
                    SUBSCRIPTION_STATUS_UPDATED_TOPIC,
                    event,
                    subscription.getId()
            );
        }
    }

    @Override
    @Transactional
    public void handlePaymentSuccess(PaymentStatusUpdated event) {
        log.info("Handling payment success event: {}", event.getEventId());

        Subscription subscription = subscriptionRepository.findById(event.getSubscriptionId())
                .orElseThrow(() -> new SubscriptionException("Subscription not found"));

        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartDate(LocalDateTime.now());
        subscription.setEndDate(LocalDateTime.now().plusMonths(1));

        subscription = subscriptionRepository.save(subscription);

        SubscriptionStatusUpdated subscriptionStatusUpdated = SubscriptionStatusUpdated.createEvent(
                subscription.getId(),
                subscription.getUserId(),
                subscription.getPlanId(),
                subscription.getStatus(),
                SubscriptionEventType.ACTIVATED,
                Map.of(
                        "reason", "PAYMENT_SUCCEEDED",
                        "reasonDescription", "Payment succeeded"
                )
        );

        outboxService.saveEvent(
                SubscriptionEventType.ACTIVATED.name(),
                SUBSCRIPTION_STATUS_UPDATED_TOPIC,
                subscriptionStatusUpdated,
                subscription.getId()
        );
    }

    @Override
    @Transactional
    public void handlePaymentFailed(PaymentStatusUpdated event) {
        log.info("Handling payment failed event: {}", event.getEventId());

        Subscription subscription = subscriptionRepository.findById(event.getSubscriptionId())
                .orElseThrow(() -> new SubscriptionException("Subscription not found"));

        subscription.setStatus(SubscriptionStatus.CANCELLED);

        subscription = subscriptionRepository.save(subscription);

        SubscriptionStatusUpdated subscriptionStatusUpdated = SubscriptionStatusUpdated.createEvent(
                subscription.getId(),
                subscription.getUserId(),
                subscription.getPlanId(),
                subscription.getStatus(),
                SubscriptionEventType.CANCELLED,
                Map.of(
                        "reason", "PAYMENT_FAILED",
                        "reasonDescription", "Payment failed"
                )
        );

        outboxService.saveEvent(
                SubscriptionEventType.CANCELLED.name(),
                SUBSCRIPTION_STATUS_UPDATED_TOPIC,
                subscriptionStatusUpdated,
                subscription.getId()
        );
    }

    private SubscriptionResponse mapToResponse(Subscription subscription) {
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .userId(subscription.getUserId())
                .planId(subscription.getPlanId())
                .status(subscription.getStatus())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .price(subscription.getPrice())
                .paymentMethodId(subscription.getPaymentMethodId())
                .createdAt(subscription.getCreatedAt())
                .updatedAt(subscription.getUpdatedAt())
                .build();
    }
} 