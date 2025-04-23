package com.subscription.configuration.kafka.consumer;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseConsumerProperties {
    private String topic;
    private String retryTopic;
    private String errorTopic;
    private String consumerGroup;
    private String consumerGroupRetry;
    private int concurrencyLevel;
    private String autoOffsetReset;
}

