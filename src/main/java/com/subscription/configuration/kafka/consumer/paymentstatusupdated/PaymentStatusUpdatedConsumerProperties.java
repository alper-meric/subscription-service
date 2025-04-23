package com.subscription.configuration.kafka.consumer.paymentstatusupdated;

import com.subscription.configuration.kafka.consumer.BaseConsumerProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.kafka.consumer.topics.payment-status-updated")
@Getter
@Setter
public class PaymentStatusUpdatedConsumerProperties extends BaseConsumerProperties {

}
