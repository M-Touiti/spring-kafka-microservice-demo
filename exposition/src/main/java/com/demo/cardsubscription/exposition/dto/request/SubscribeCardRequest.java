package com.demo.cardsubscription.exposition.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * REST request body for subscribing a new card.
 * Uses a Java record for immutability and conciseness.
 */
public record SubscribeCardRequest(

        @NotBlank(message = "cardId must not be blank")
        @Size(max = 64, message = "cardId must not exceed 64 characters")
        String cardId,

        @NotBlank(message = "userId must not be blank")
        @Size(max = 64, message = "userId must not exceed 64 characters")
        String userId
) {
}
