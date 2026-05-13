package com.demo.cardsubscription.infrastructure.persistence.repository;

import com.demo.cardsubscription.infrastructure.persistence.entity.CardSubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardSubscriptionJpaRepository extends JpaRepository<CardSubscriptionEntity, String> {

    Optional<CardSubscriptionEntity> findByCardId(String cardId);
}
