package com.subscription.scheduler;

import com.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionScheduler {

    private final SubscriptionService subscriptionService;

    // Her gün gece yarısı çalışır
    @Scheduled(cron = "0 0 0 * * ?")
    public void processExpiredSubscriptions() {
        log.info("Starting expired subscriptions process");
        subscriptionService.processExpiredSubscriptions();
    }

    // Her gün gece yarısı çalışır
    @Scheduled(cron = "0 0 0 * * ?")
    public void processRenewalSubscriptions() {
        log.info("Starting subscription renewals process");
        subscriptionService.processRenewalSubscriptions();
    }
} 