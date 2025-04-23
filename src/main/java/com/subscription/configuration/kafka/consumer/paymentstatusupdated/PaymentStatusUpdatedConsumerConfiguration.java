package com.subscription.configuration.kafka.consumer.paymentstatusupdated;

import com.subscription.configuration.kafka.consumer.BaseConsumerConfiguration;
import com.subscription.model.event.PaymentStatusUpdated;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class PaymentStatusUpdatedConsumerConfiguration extends BaseConsumerConfiguration {

    private final PaymentStatusUpdatedConsumerProperties properties;

    @Value("${spring.kafka.bootstrap-servers}")
    private String host;

    public PaymentStatusUpdatedConsumerConfiguration(PaymentStatusUpdatedConsumerProperties properties) {
        this.properties = properties;
    }

    @Bean
    public ProducerFactory<String, PaymentStatusUpdated> paymentStatusUpdatedProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, host);
        configProps.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(org.apache.kafka.clients.producer.ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(org.apache.kafka.clients.producer.ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, PaymentStatusUpdated> paymentStatusUpdatedProducerFactoryKafkaTemplate() {
        return new KafkaTemplate<>(paymentStatusUpdatedProducerFactory());
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
