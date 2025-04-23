package com.subscription.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subscription.model.OutboxEvent;
import com.subscription.repository.OutboxEventRepository;
import com.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public void saveEvent(String eventType, String topic, Object payload, UUID subscriptionId) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            OutboxEvent event = OutboxEvent.builder()
                    .eventType(eventType)
                    .topic(topic)
                    .payload(payloadJson)
                    .processed(false)
                    .subscriptionId(subscriptionId)
                    .build();
            event = outboxEventRepository.save(event);
            log.info("Saved outbox event: {} for subscription: {}", event.getId(), subscriptionId);
        } catch (Exception e) {
            log.error("Error saving outbox event", e);
            throw new RuntimeException("Error saving outbox event", e);
        }
    }

    @Transactional
    public void processEvents() {
        var events = outboxEventRepository.findUnprocessedEvents();
        log.info("Found {} unprocessed events", events.size());
        
        for (OutboxEvent event : events) {
            try {
                log.info("Processing event: {} for subscription: {}", event.getId(), event.getSubscriptionId());
                
                boolean subscriptionExists = subscriptionRepository.existsById(event.getSubscriptionId());
                if (!subscriptionExists) {
                    log.warn("Subscription not found for event: {}, skipping", event.getId());
                    event.setProcessed(true);
                    outboxEventRepository.save(event);
                    continue;
                }

                sendToKafka(event);
                // Kafka'ya başarıyla gönderildikten sonra processed yapıyoruz
                // @UpdateTimestamp sayesinde updatedAt otomatik güncellenecek
                event.setProcessed(true);
                outboxEventRepository.save(event);
                log.info("Successfully processed outbox event: {} at {}", event.getId(), event.getUpdatedAt());
            } catch (Exception e) {
                log.error("Error processing outbox event: {}", event.getId(), e);
            }
        }
    }

    private void sendToKafka(OutboxEvent event) {
        try {
            log.info("Sending event to Kafka - Topic: {}, Key: {}, Payload: {}", 
                    event.getTopic(), 
                    event.getSubscriptionId(), 
                    event.getPayload());
                    
            kafkaTemplate.send(event.getTopic(), event.getSubscriptionId().toString(), event.getPayload());
            log.info("Successfully sent event to Kafka: {}", event.getId());
        } catch (Exception e) {
            log.error("Failed to send event to Kafka: {}", event.getId(), e);
            throw new RuntimeException("Failed to send event to Kafka", e);
        }
    }
} 