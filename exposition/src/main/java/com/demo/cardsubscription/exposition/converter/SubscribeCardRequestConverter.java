package com.demo.cardsubscription.exposition.converter;

import com.demo.cardsubscription.application.command.SubscribeCardCommand;
import com.demo.cardsubscription.exposition.dto.request.SubscribeCardRequest;
import org.springframework.stereotype.Component;

/**
 * Converts a {@link SubscribeCardRequest} REST DTO to a {@link SubscribeCardCommand}.
 *
 * <p>This anti-corruption layer ensures the controller never constructs domain/application
 * objects directly, and that the mapping logic is isolated and independently testable.
 */
@Component
public class SubscribeCardRequestConverter {

    public SubscribeCardCommand toCommand(SubscribeCardRequest request) {
        return new SubscribeCardCommand(request.cardId(), request.userId());
    }
}
