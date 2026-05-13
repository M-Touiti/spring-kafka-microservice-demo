package com.demo.cardsubscription.infrastructure.persistence.adapter;

import com.demo.cardsubscription.application.port.out.LoadCardSubscriptionPort;
import com.demo.cardsubscription.application.port.out.SaveCardSubscriptionPort;
import com.demo.cardsubscription.domain.model.CardSubscription;
import com.demo.cardsubscription.infrastructure.persistence.entity.CardSubscriptionEntity;
import com.demo.cardsubscription.infrastructure.persistence.repository.CardSubscriptionJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Persistence adapter implementing both outbound ports.
 * Translates between domain model and JPA entity using MapStruct.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CardSubscriptionPersistenceAdapter implements SaveCardSubscriptionPort, LoadCardSubscriptionPort {

    private final CardSubscriptionJpaRepository repository;
    private final CardSubscriptionEntityMapper mapper;

    // ─── SaveCardSubscriptionPort ─────────────────────────────────────────────

    @Override
    public CardSubscription save(CardSubscription subscription) {
        CardSubscriptionEntity entity = mapper.toEntity(subscription);
        CardSubscriptionEntity saved = repository.save(entity);
        log.debug("Persisted subscription id={} status={}", saved.getId(), saved.getStatus());
        return mapper.toDomain(saved);
    }

    // ─── LoadCardSubscriptionPort ─────────────────────────────────────────────

    @Override
    public Optional<CardSubscription> findById(String id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<CardSubscription> findByCardId(String cardId) {
        return repository.findByCardId(cardId).map(mapper::toDomain);
    }
}
