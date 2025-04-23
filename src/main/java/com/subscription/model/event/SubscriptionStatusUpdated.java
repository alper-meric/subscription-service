package com.subscription.model.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.subscription.model.enums.SubscriptionStatus;
import com.subscription.model.enums.SubscriptionEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionStatusUpdated {
    private String eventId;
    private UUID subscriptionId;
    private UUID userId;
    private Long planId;
    private SubscriptionStatus status;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime timestamp;
    private SubscriptionEventType eventType;
    private Map<String, Object> metadata;

    public static SubscriptionStatusUpdated createEvent(
            UUID subscriptionId,
            UUID userId,
            Long planId,
            SubscriptionStatus status,
            SubscriptionEventType eventType,
            Map<String, Object> metadata) {
        
        return SubscriptionStatusUpdated.builder()
                .eventId(UUID.randomUUID().toString())
                .subscriptionId(subscriptionId)
                .userId(userId)
                .planId(planId)
                .status(status)
                .eventType(eventType)
                .metadata(metadata)
                .timestamp(LocalDateTime.now())
                .build();
    }
} 