package com.subscription.consumer;

import com.subscription.model.event.PaymentStatusUpdated;
import com.subscription.model.enums.PaymentEventType;
import com.subscription.strategy.PaymentEventStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PaymentStatusUpdatedConsumer {

    private final Map<PaymentEventType, PaymentEventStrategy> strategies;

    public PaymentStatusUpdatedConsumer(List<PaymentEventStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        PaymentEventStrategy::getEventType,
                        Function.identity()
                ));
    }

    @KafkaListener(
            topics = "${spring.kafka.consumer.topics.payment-status-updated.topic}",
            groupId = "${spring.kafka.consumer.topics.payment-status-updated.consumerGroup}",
            containerFactory = "paymentStatusUpdatedKafkaListenerContainerFactory"
    )
    public void consumePaymentStatusUpdatedEvent(@Payload PaymentStatusUpdated eventData,
                                                 @Headers ConsumerRecord<String, Object> consumerRecord) {
        consumePaymentEvent(eventData, consumerRecord, "consumePaymentStatusUpdatedEvent");
    }

    @KafkaListener(
            topics = "${spring.kafka.consumer.topics.payment-status-updated.retryTopic}",
            groupId = "${spring.kafka.consumer.topics.payment-status-updated.consumerGroupRetry}",
            containerFactory = "paymentStatusUpdatedKafkaRetryListenerContainerFactory"
    )
    public void retryPaymentStatusUpdatedEvent(@Payload PaymentStatusUpdated eventData,
                                               @Headers ConsumerRecord<String, Object> consumerRecord) {
        consumePaymentEvent(eventData, consumerRecord, "retryPaymentStatusUpdatedEvent");
    }

    public void consumePaymentEvent(
            @Payload PaymentStatusUpdated event,
            @Headers ConsumerRecord<String, Object> consumerRecord,
            String methodName) {
        try {
            log.info("Received payment event: {} for subscription: {}",
                    event.getEventId(),
                    event.getSubscriptionId()
            );

            PaymentEventStrategy strategy = strategies.get(event.getEventType());
            if (strategy == null) {
                log.error("No strategy found for payment event type: {} for event: {} in topic: {}, partition: {}, offset: {}",
                        event.getEventType(),
                        event.getEventId(),
                        consumerRecord.topic(),
                        consumerRecord.partition(),
                        consumerRecord.offset()
                );
                return;
            }

            strategy.handle(event);
            log.info("Successfully processed payment event: {} for subscription: {}",
                    event.getEventId(),
                    event.getSubscriptionId()
            );

        } catch (Exception e) {
            log.error("Error processing payment event: {} for subscription: {} in topic: {}, partition: {}, offset: {}",
                    event.getEventId(),
                    event.getSubscriptionId(),
                    consumerRecord.topic(),
                    consumerRecord.partition(),
                    consumerRecord.offset(),
                    e
            );
            throw e;
        }
    }
} 