package com.demo.cardsubscription.application.port.in;

import com.demo.cardsubscription.domain.model.CardSubscription;

/**
 * Inbound port: query existing subscriptions.
 * Driven by the REST controller.
 */
public interface GetCardSubscriptionUseCase {

    CardSubscription getById(String id);

    CardSubscription getByCardId(String cardId);
}
