package com.demo.cardsubscription.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Kafka message DTO for card events consumed from the topic.
 * This is an infrastructure concern and lives only in the kafka-consumer module.
 * It is mapped to a {@link com.demo.cardsubscription.application.command.SubscribeCardCommand}
 * before being passed to the application layer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardEventMessage {

    private String eventId;
    private String eventType;
    private String cardId;
    private String userId;
    private LocalDateTime eventTimestamp;
}
