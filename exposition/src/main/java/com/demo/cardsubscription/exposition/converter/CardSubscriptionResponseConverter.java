package com.demo.cardsubscription.exposition.converter;

import com.demo.cardsubscription.domain.model.CardSubscription;
import com.demo.cardsubscription.exposition.dto.response.CardSubscriptionResponse;
import org.springframework.stereotype.Component;

/**
 * Converts a {@link CardSubscription} domain object to a {@link CardSubscriptionResponse} DTO.
 *
 * <p>Keeping this conversion in a dedicated class makes it:
 * <ul>
 *   <li>Independently testable without needing the full controller context</li>
 *   <li>Easy to extend (e.g., field masking, localisation) without touching the controller</li>
 * </ul>
 */
@Component
public class CardSubscriptionResponseConverter {

    public CardSubscriptionResponse toResponse(CardSubscription domain) {
        return new CardSubscriptionResponse(
                domain.getId(),
                domain.getCardId(),
                domain.getUserId(),
                domain.getStatus().name(),
                domain.getRequestedAt(),
                domain.getProcessedAt(),
                domain.getErrorMessage()
        );
    }
}
