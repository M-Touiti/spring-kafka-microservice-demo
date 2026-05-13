package com.demo.cardsubscription.domain.exception;

public class CardSubscriptionNotFoundException extends RuntimeException {

    public CardSubscriptionNotFoundException(String message) {
        super(message);
    }

    public static CardSubscriptionNotFoundException forCardId(String cardId) {
        return new CardSubscriptionNotFoundException("No subscription found for cardId: " + cardId);
    }

    public static CardSubscriptionNotFoundException forId(String id) {
        return new CardSubscriptionNotFoundException("Subscription not found with id: " + id);
    }
}
