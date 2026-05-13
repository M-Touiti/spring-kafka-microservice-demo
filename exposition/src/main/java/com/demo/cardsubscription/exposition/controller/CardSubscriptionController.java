package com.demo.cardsubscription.exposition.controller;

import com.demo.cardsubscription.application.command.SubscribeCardCommand;
import com.demo.cardsubscription.application.port.in.GetCardSubscriptionUseCase;
import com.demo.cardsubscription.application.port.in.SubscribeCardUseCase;
import com.demo.cardsubscription.domain.model.CardSubscription;
import com.demo.cardsubscription.exposition.converter.CardSubscriptionResponseConverter;
import com.demo.cardsubscription.exposition.converter.SubscribeCardRequestConverter;
import com.demo.cardsubscription.exposition.dto.request.SubscribeCardRequest;
import com.demo.cardsubscription.exposition.dto.response.CardSubscriptionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for card subscription operations.
 *
 * <p>The controller is intentionally thin: it delegates all business logic to
 * use-case ports and all conversion to dedicated converter classes.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/card-subscriptions")
@RequiredArgsConstructor
public class CardSubscriptionController {

    private final SubscribeCardUseCase subscribeCardUseCase;
    private final GetCardSubscriptionUseCase getCardSubscriptionUseCase;
    private final SubscribeCardRequestConverter requestConverter;
    private final CardSubscriptionResponseConverter responseConverter;

    /**
     * Subscribe a new card.
     * POST /api/v1/card-subscriptions
     */
    @PostMapping
    public ResponseEntity<CardSubscriptionResponse> subscribe(
            @RequestBody @Valid SubscribeCardRequest request) {

        log.info("POST /card-subscriptions | cardId={}", request.cardId());

        SubscribeCardCommand command = requestConverter.toCommand(request);
        CardSubscription subscription = subscribeCardUseCase.subscribe(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(responseConverter.toResponse(subscription));
    }

    /**
     * Get a subscription by its internal ID.
     * GET /api/v1/card-subscriptions/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<CardSubscriptionResponse> getById(@PathVariable String id) {
        log.info("GET /card-subscriptions/{}", id);
        CardSubscription subscription = getCardSubscriptionUseCase.getById(id);
        return ResponseEntity.ok(responseConverter.toResponse(subscription));
    }

    /**
     * Get a subscription by card ID.
     * GET /api/v1/card-subscriptions/card/{cardId}
     */
    @GetMapping("/card/{cardId}")
    public ResponseEntity<CardSubscriptionResponse> getByCardId(@PathVariable String cardId) {
        log.info("GET /card-subscriptions/card/{}", cardId);
        CardSubscription subscription = getCardSubscriptionUseCase.getByCardId(cardId);
        return ResponseEntity.ok(responseConverter.toResponse(subscription));
    }
}
