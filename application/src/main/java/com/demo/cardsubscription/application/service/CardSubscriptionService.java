package com.demo.cardsubscription.application.service;

import com.demo.cardsubscription.application.command.SubscribeCardCommand;
import com.demo.cardsubscription.application.port.in.GetCardSubscriptionUseCase;
import com.demo.cardsubscription.application.port.in.SubscribeCardUseCase;
import com.demo.cardsubscription.application.port.out.CardApiPort;
import com.demo.cardsubscription.application.port.out.LoadCardSubscriptionPort;
import com.demo.cardsubscription.application.port.out.SaveCardSubscriptionPort;
import com.demo.cardsubscription.domain.exception.CardSubscriptionNotFoundException;
import com.demo.cardsubscription.domain.model.CardSubscription;
import com.demo.cardsubscription.domain.model.SubscriptionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service implementing all card-subscription use cases.
 * Orchestrates domain logic, outbound ports, and external API calls.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CardSubscriptionService implements SubscribeCardUseCase, GetCardSubscriptionUseCase {

    private final SaveCardSubscriptionPort saveCardSubscriptionPort;
    private final LoadCardSubscriptionPort loadCardSubscriptionPort;
    private final CardApiPort cardApiPort;

    // ─── SubscribeCardUseCase ──────────────────────────────────────────────────

    @Override
    public CardSubscription subscribe(SubscribeCardCommand command) {
        log.info("Processing subscription for cardId={}, userId={}", command.cardId(), command.userId());

        // Idempotency: if already subscribed, return existing record
        Optional<CardSubscription> existing = loadCardSubscriptionPort.findByCardId(command.cardId());
        if (existing.isPresent() && existing.get().isActive()) {
            log.info("Card {} is already subscribed – skipping", command.cardId());
            return existing.get();
        }

        // 1. Persist a PENDING entry first
        CardSubscription subscription = CardSubscription.builder()
                .id(UUID.randomUUID().toString())
                .cardId(command.cardId())
                .userId(command.userId())
                .status(SubscriptionStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .build();

        CardSubscription saved = saveCardSubscriptionPort.save(subscription);

        // 2. Call external API and update status
        try {
            boolean success = cardApiPort.subscribeCard(command.cardId(), command.userId());
            if (success) {
                saved.markSubscribed();
                log.info("Card {} subscribed successfully", command.cardId());
            } else {
                saved.markFailed("External API returned a non-success response");
                log.warn("Card {} subscription failed – API returned failure", command.cardId());
            }
        } catch (Exception ex) {
            log.error("External API error for cardId={}: {}", command.cardId(), ex.getMessage(), ex);
            saved.markFailed(ex.getMessage());
        }

        return saveCardSubscriptionPort.save(saved);
    }

    // ─── GetCardSubscriptionUseCase ───────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public CardSubscription getById(String id) {
        return loadCardSubscriptionPort.findById(id)
                .orElseThrow(() -> CardSubscriptionNotFoundException.forId(id));
    }

    @Override
    @Transactional(readOnly = true)
    public CardSubscription getByCardId(String cardId) {
        return loadCardSubscriptionPort.findByCardId(cardId)
                .orElseThrow(() -> CardSubscriptionNotFoundException.forCardId(cardId));
    }
}
