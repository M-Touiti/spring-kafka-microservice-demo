package com.demo.cardsubscription.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Aggregate root representing a card subscription.
 *
 * <p>Pure domain object — zero framework or annotation-processor dependencies.
 * Getters, setters, equals/hashCode, and the builder are all hand-written so
 * this module compiles with a plain {@code javac} and carries no runtime baggage.
 */
public class CardSubscription {

    private String id;
    private String cardId;
    private String userId;
    private SubscriptionStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private String errorMessage;

    // ─── Private constructor – use builder() ─────────────────────────────────

    private CardSubscription() {}

    // ─── Business behaviour ───────────────────────────────────────────────────

    /** A subscription is considered active only when SUBSCRIBED. */
    public boolean isActive() {
        return SubscriptionStatus.SUBSCRIBED == this.status;
    }

    /** Mark the subscription as successfully processed. */
    public void markSubscribed() {
        this.status = SubscriptionStatus.SUBSCRIBED;
        this.processedAt = LocalDateTime.now();
        this.errorMessage = null;
    }

    /** Mark the subscription as failed with a human-readable reason. */
    public void markFailed(String reason) {
        this.status = SubscriptionStatus.FAILED;
        this.processedAt = LocalDateTime.now();
        this.errorMessage = reason;
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public String getId()                   { return id; }
    public String getCardId()               { return cardId; }
    public String getUserId()               { return userId; }
    public SubscriptionStatus getStatus()   { return status; }
    public LocalDateTime getRequestedAt()   { return requestedAt; }
    public LocalDateTime getProcessedAt()   { return processedAt; }
    public String getErrorMessage()         { return errorMessage; }

    // ─── Setters ──────────────────────────────────────────────────────────────

    public void setId(String id)                            { this.id = id; }
    public void setCardId(String cardId)                    { this.cardId = cardId; }
    public void setUserId(String userId)                    { this.userId = userId; }
    public void setStatus(SubscriptionStatus status)        { this.status = status; }
    public void setRequestedAt(LocalDateTime requestedAt)   { this.requestedAt = requestedAt; }
    public void setProcessedAt(LocalDateTime processedAt)   { this.processedAt = processedAt; }
    public void setErrorMessage(String errorMessage)        { this.errorMessage = errorMessage; }

    // ─── equals / hashCode (identity by id) ──────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CardSubscription that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "CardSubscription{id='" + id + "', cardId='" + cardId +
               "', userId='" + userId + "', status=" + status + "}";
    }

    // ─── Builder ──────────────────────────────────────────────────────────────

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String id;
        private String cardId;
        private String userId;
        private SubscriptionStatus status;
        private LocalDateTime requestedAt;
        private LocalDateTime processedAt;
        private String errorMessage;

        private Builder() {}

        public Builder id(String id)                        { this.id = id;                     return this; }
        public Builder cardId(String cardId)                { this.cardId = cardId;             return this; }
        public Builder userId(String userId)                { this.userId = userId;             return this; }
        public Builder status(SubscriptionStatus status)    { this.status = status;             return this; }
        public Builder requestedAt(LocalDateTime v)         { this.requestedAt = v;             return this; }
        public Builder processedAt(LocalDateTime v)         { this.processedAt = v;             return this; }
        public Builder errorMessage(String errorMessage)    { this.errorMessage = errorMessage; return this; }

        public CardSubscription build() {
            CardSubscription s = new CardSubscription();
            s.id           = this.id;
            s.cardId       = this.cardId;
            s.userId       = this.userId;
            s.status       = this.status;
            s.requestedAt  = this.requestedAt;
            s.processedAt  = this.processedAt;
            s.errorMessage = this.errorMessage;
            return s;
        }
    }
}
