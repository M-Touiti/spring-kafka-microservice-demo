package com.demo.cardsubscription.infrastructure.persistence.adapter;

import com.demo.cardsubscription.domain.model.CardSubscription;
import com.demo.cardsubscription.domain.model.SubscriptionStatus;
import com.demo.cardsubscription.infrastructure.persistence.entity.CardSubscriptionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper to convert between the JPA entity and the domain model.
 * Keeps the domain free of any persistence annotations.
 */
@Mapper(componentModel = "spring")
public interface CardSubscriptionEntityMapper {

    @Mapping(target = "status", expression = "java(entity.getStatus())")
    CardSubscription toDomain(CardSubscriptionEntity entity);

    @Mapping(target = "status", expression = "java(domain.getStatus().name())")
    CardSubscriptionEntity toEntity(CardSubscription domain);

    default SubscriptionStatus map(String status) {
        return SubscriptionStatus.valueOf(status);
    }
}
