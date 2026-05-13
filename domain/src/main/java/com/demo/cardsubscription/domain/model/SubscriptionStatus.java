package com.demo.cardsubscription.domain.model;

public enum SubscriptionStatus {
    /** Initial state – persisted but external API not yet called. */
    PENDING,
    /** External API confirmed the subscription. */
    SUBSCRIBED,
    /** External API call failed or returned a non-success response. */
    FAILED
}
