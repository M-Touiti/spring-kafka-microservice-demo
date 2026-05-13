package com.demo.cardsubscription.application.port.in;

import com.demo.cardsubscription.application.command.SubscribeCardCommand;
import com.demo.cardsubscription.domain.model.CardSubscription;

/**
 * Inbound port: subscribe a new card.
 * Driven by Kafka consumer or REST controller.
 */
public interface SubscribeCardUseCase {

    CardSubscription subscribe(SubscribeCardCommand command);
}
