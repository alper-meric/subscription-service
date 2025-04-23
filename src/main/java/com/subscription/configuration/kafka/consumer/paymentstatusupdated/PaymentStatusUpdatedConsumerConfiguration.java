package com.subscription.configuration.kafka.consumer.paymentstatusupdated;

import com.subscription.configuration.kafka.consumer.BaseConsumerConfiguration;
import com.subscription.model.event.PaymentStatusUpdated;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class PaymentStatusUpdatedConsumerConfiguration extends BaseConsumerConfiguration {

    private final PaymentStatusUpdatedConsumerProperties properties;

    @Value("${spring.kafka.bootstrap-servers}")
    private String host;

    public PaymentStatusUpdatedConsumerConfiguration(PaymentStatusUpdatedConsumerProperties properties) {
        this.properties = properties;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentStatusUpdated> paymentStatusUpdatedKafkaListenerContainerFactory(
            final KafkaTemplate<String, PaymentStatusUpdated> kafkaTemplate) {
        return baseKafkaListenerContainerFactory(
                kafkaTemplate,
                host,
                properties.getAutoOffsetReset(),
                properties.getRetryTopic(),
                properties.getConcurrencyLevel(),
                properties.getConsumerGroup(),
                PaymentStatusUpdated.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentStatusUpdated> paymentStatusUpdatedKafkaRetryListenerContainerFactory(
            final KafkaTemplate<String, PaymentStatusUpdated> kafkaTemplate) {
        return baseKafkaRetryListenerContainerFactory(
                kafkaTemplate,
                host,
                properties.getAutoOffsetReset(),
                properties.getErrorTopic(),
                properties.getConcurrencyLevel(),
                properties.getConsumerGroupRetry(),
                PaymentStatusUpdated.class);
    }
}
