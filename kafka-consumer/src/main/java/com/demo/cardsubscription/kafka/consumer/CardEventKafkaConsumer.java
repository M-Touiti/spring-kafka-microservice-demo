package com.demo.cardsubscription.kafka.consumer;

import com.demo.cardsubscription.application.command.SubscribeCardCommand;
import com.demo.cardsubscription.application.port.in.SubscribeCardUseCase;
import com.demo.cardsubscription.kafka.dto.CardEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for card events.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Receive and deserialise the {@link CardEventMessage}</li>
 *   <li>Map it to a domain command (thin anti-corruption layer)</li>
 *   <li>Delegate business logic to {@link SubscribeCardUseCase}</li>
 * </ul>
 *
 * <p>Retry and Dead Letter Topic handling is centralised in
 * {@link com.demo.cardsubscription.kafka.config.KafkaConsumerConfig}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CardEventKafkaConsumer {

    private final SubscribeCardUseCase subscribeCardUseCase;

    @KafkaListener(
            topics = "${kafka.topics.card-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "cardEventKafkaListenerContainerFactory"
    )
    public void consume(
            @Payload CardEventMessage message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Received card event | eventId={} cardId={} userId={} topic={} partition={} offset={}",
                message.getEventId(), message.getCardId(), message.getUserId(),
                topic, partition, offset);

        SubscribeCardCommand command = toCommand(message);
        subscribeCardUseCase.subscribe(command);

        log.info("Card event processed successfully | eventId={}", message.getEventId());
    }

    /**
     * Maps the Kafka message DTO to an application command.
     * This is the anti-corruption layer between messaging and application concerns.
     */
    private SubscribeCardCommand toCommand(CardEventMessage message) {
        return new SubscribeCardCommand(message.getCardId(), message.getUserId());
    }
}
