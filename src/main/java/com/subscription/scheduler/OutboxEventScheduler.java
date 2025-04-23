package com.subscription.scheduler;

import com.subscription.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventScheduler {

    private final OutboxService outboxService;

    @Scheduled(fixedRate = 5000) // Her 5 saniyede bir çalışır
    public void processOutboxEvents() {
        log.info("Starting outbox event processing at: {}", System.currentTimeMillis());
        try {
            outboxService.processEvents();
            log.info("Finished outbox event processing at: {}", System.currentTimeMillis());
        } catch (Exception e) {
            log.error("Error processing outbox events", e);
        }
    }
} 