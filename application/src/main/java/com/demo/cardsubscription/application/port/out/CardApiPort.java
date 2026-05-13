package com.demo.cardsubscription.application.port.out;

/**
 * Outbound port: call the external card subscription API.
 * Implemented by the infrastructure HTTP adapter.
 */
public interface CardApiPort {

    /**
     * Subscribes a card via the external API.
     *
     * @param cardId the card identifier
     * @param userId the user identifier
     * @return true if the external API confirmed success
     * @throws com.demo.cardsubscription.domain.exception.CardSubscriptionNotFoundException
     *         or a runtime exception if the call fails
     */
    boolean subscribeCard(String cardId, String userId);
}
