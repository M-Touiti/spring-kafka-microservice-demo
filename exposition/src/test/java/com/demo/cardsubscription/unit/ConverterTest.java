package com.demo.cardsubscription.unit;

import com.demo.cardsubscription.application.command.SubscribeCardCommand;
import com.demo.cardsubscription.domain.model.CardSubscription;
import com.demo.cardsubscription.domain.model.SubscriptionStatus;
import com.demo.cardsubscription.exposition.converter.CardSubscriptionResponseConverter;
import com.demo.cardsubscription.exposition.converter.SubscribeCardRequestConverter;
import com.demo.cardsubscription.exposition.dto.request.SubscribeCardRequest;
import com.demo.cardsubscription.exposition.dto.response.CardSubscriptionResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ConverterTest {

    private final CardSubscriptionResponseConverter responseConverter = new CardSubscriptionResponseConverter();
    private final SubscribeCardRequestConverter requestConverter = new SubscribeCardRequestConverter();

    @Test
    @DisplayName("responseConverter – should map all domain fields to response DTO")
    void toResponse_mapsAllFields() {
        LocalDateTime now = LocalDateTime.now();
        CardSubscription domain = CardSubscription.builder()
                .id("id-1")
                .cardId("card-1")
                .userId("user-1")
                .status(SubscriptionStatus.SUBSCRIBED)
                .requestedAt(now)
                .processedAt(now)
                .build();

        CardSubscriptionResponse response = responseConverter.toResponse(domain);

        assertThat(response.id()).isEqualTo("id-1");
        assertThat(response.cardId()).isEqualTo("card-1");
        assertThat(response.userId()).isEqualTo("user-1");
        assertThat(response.status()).isEqualTo("SUBSCRIBED");
        assertThat(response.requestedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("requestConverter – should map request DTO to command")
    void toCommand_mapsFields() {
        SubscribeCardRequest request = new SubscribeCardRequest("card-99", "user-88");

        SubscribeCardCommand command = requestConverter.toCommand(request);

        assertThat(command.cardId()).isEqualTo("card-99");
        assertThat(command.userId()).isEqualTo("user-88");
    }
}
