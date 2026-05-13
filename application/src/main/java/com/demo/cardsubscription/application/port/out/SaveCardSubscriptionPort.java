package com.demo.cardsubscription.application.port.out;

import com.demo.cardsubscription.domain.model.CardSubscription;

/**
 * Outbound port: persist a card subscription.
 * Implemented by the infrastructure persistence adapter.
 */
public interface SaveCardSubscriptionPort {

    CardSubscription save(CardSubscription subscription);
}
