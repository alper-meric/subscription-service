package com.subscription.configuration.kafka.consumer.paymentstatusupdated;

import com.subscription.configuration.kafka.consumer.BaseConsumerConfiguration;
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
    public ConcurrentKafkaListenerContainerFactory<String, String> paymentStatusUpdatedKafkaListenerContainerFactory(
            final KafkaTemplate<String, String> kafkaTemplate) {
        return baseKafkaListenerContainerFactory(
                kafkaTemplate,
                host,
                properties.getAutoOffsetReset(),
                properties.getRetryTopic(),
                properties.getConcurrencyLevel(),
                properties.getConsumerGroup(),
                String.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> paymentStatusUpdatedKafkaRetryListenerContainerFactory(
            final KafkaTemplate<String, String> kafkaTemplate) {
        return baseKafkaRetryListenerContainerFactory(
                kafkaTemplate,
                host,
                properties.getAutoOffsetReset(),
                properties.getErrorTopic(),
                properties.getConcurrencyLevel(),
                properties.getConsumerGroupRetry(),
                String.class);
    }
}
