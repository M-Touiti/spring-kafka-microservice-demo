package com.demo.cardsubscription.application.command;

/**
 * Command object carrying the intent to subscribe a new card.
 * Using a Java record to make it immutable by design.
 */
public record SubscribeCardCommand(String cardId, String userId) {

    public SubscribeCardCommand {
        if (cardId == null || cardId.isBlank()) throw new IllegalArgumentException("cardId must not be blank");
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("userId must not be blank");
    }
}
