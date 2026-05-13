package com.demo.cardsubscription.exposition.dto.response;

import java.time.LocalDateTime;

/**
 * REST response body representing a card subscription.
 * Uses a Java record – serialised to JSON by Jackson automatically.
 */
public record CardSubscriptionResponse(
        String id,
        String cardId,
        String userId,
        String status,
        LocalDateTime requestedAt,
        LocalDateTime processedAt,
        String errorMessage
) {
}
