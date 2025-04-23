package com.subscription.configuration.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

public class BaseConsumerConfiguration {

    public <T> ConsumerFactory<String, T> baseConsumerFactory(String kafkaHost, String autoOffsetReset, String consumerGroup, Class<T> valueType) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 120000);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 210000);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.subscription.model.event");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, valueType.getName());

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>(valueType, false)
        );
    }

    public <T> ConcurrentKafkaListenerContainerFactory<String, T> baseKafkaListenerContainerFactory(
            KafkaOperations<String, T> kafkaTemplate,
            String kafkaHost,
            String autoOffsetReset,
            String errorTopic,
            int concurrencyLevel,
            String consumerGroup,
            Class<T> valueType) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(baseConsumerFactory(kafkaHost, autoOffsetReset, consumerGroup, valueType));
        factory.setCommonErrorHandler(new DefaultErrorHandler(
                new DeadLetterPublishingRecoverer(kafkaTemplate, (cr, ex) ->
                        new TopicPartition(errorTopic, -1)),
                new FixedBackOff(0, 0)
        ));
        factory.setConcurrency(concurrencyLevel);

        return factory;
    }

    public <T> ConcurrentKafkaListenerContainerFactory<String, T> baseKafkaRetryListenerContainerFactory(
            KafkaOperations<String, T> kafkaTemplate,
            String kafkaHost,
            String autoOffsetReset,
            String errorTopic,
            int concurrencyLevel,
            String consumerGroupRetry,
            Class<T> valueType) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(baseConsumerFactory(kafkaHost, autoOffsetReset, consumerGroupRetry, valueType));
        factory.setCommonErrorHandler(new DefaultErrorHandler(
                new DeadLetterPublishingRecoverer(kafkaTemplate, (cr, ex) ->
                        new TopicPartition(errorTopic, -1)),
                new FixedBackOff(3000, 3)
        ));
        factory.setConcurrency(concurrencyLevel);

        return factory;
    }
}

