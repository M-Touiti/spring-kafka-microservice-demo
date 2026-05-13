package com.demo.cardsubscription.unit;

import com.demo.cardsubscription.application.command.SubscribeCardCommand;
import com.demo.cardsubscription.application.port.out.CardApiPort;
import com.demo.cardsubscription.application.port.out.LoadCardSubscriptionPort;
import com.demo.cardsubscription.application.port.out.SaveCardSubscriptionPort;
import com.demo.cardsubscription.application.service.CardSubscriptionService;
import com.demo.cardsubscription.domain.exception.CardSubscriptionNotFoundException;
import com.demo.cardsubscription.domain.model.CardSubscription;
import com.demo.cardsubscription.domain.model.SubscriptionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardSubscriptionServiceTest {

    @Mock
    private SaveCardSubscriptionPort saveCardSubscriptionPort;

    @Mock
    private LoadCardSubscriptionPort loadCardSubscriptionPort;

    @Mock
    private CardApiPort cardApiPort;

    @InjectMocks
    private CardSubscriptionService service;

    private SubscribeCardCommand command;

    @BeforeEach
    void setUp() {
        command = new SubscribeCardCommand("card-123", "user-456");
    }

    @Test
    @DisplayName("subscribe – happy path – should persist SUBSCRIBED status")
    void subscribe_success() {
        // given
        when(loadCardSubscriptionPort.findByCardId("card-123")).thenReturn(Optional.empty());
        when(saveCardSubscriptionPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(cardApiPort.subscribeCard("card-123", "user-456")).thenReturn(true);

        // when
        CardSubscription result = service.subscribe(command);

        // then
        assertThat(result.getStatus()).isEqualTo(SubscriptionStatus.SUBSCRIBED);
        assertThat(result.getProcessedAt()).isNotNull();
        verify(saveCardSubscriptionPort, times(2)).save(any()); // PENDING then SUBSCRIBED
    }

    @Test
    @DisplayName("subscribe – external API returns false – should persist FAILED status")
    void subscribe_apiFailure() {
        when(loadCardSubscriptionPort.findByCardId(any())).thenReturn(Optional.empty());
        when(saveCardSubscriptionPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(cardApiPort.subscribeCard(any(), any())).thenReturn(false);

        CardSubscription result = service.subscribe(command);

        assertThat(result.getStatus()).isEqualTo(SubscriptionStatus.FAILED);
        assertThat(result.getErrorMessage()).isNotBlank();
    }

    @Test
    @DisplayName("subscribe – card already active – should return existing record without calling API")
    void subscribe_idempotent() {
        CardSubscription existing = CardSubscription.builder()
                .id("existing-id")
                .cardId("card-123")
                .userId("user-456")
                .status(SubscriptionStatus.SUBSCRIBED)
                .requestedAt(LocalDateTime.now())
                .build();

        when(loadCardSubscriptionPort.findByCardId("card-123")).thenReturn(Optional.of(existing));

        CardSubscription result = service.subscribe(command);

        assertThat(result.getId()).isEqualTo("existing-id");
        verifyNoInteractions(cardApiPort);
        verify(saveCardSubscriptionPort, never()).save(any());
    }

    @Test
    @DisplayName("getByCardId – not found – should throw CardSubscriptionNotFoundException")
    void getByCardId_notFound() {
        when(loadCardSubscriptionPort.findByCardId("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByCardId("unknown"))
                .isInstanceOf(CardSubscriptionNotFoundException.class)
                .hasMessageContaining("unknown");
    }
}
