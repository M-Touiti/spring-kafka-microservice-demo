package com.demo.cardsubscription.infrastructure.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO mapping the response body of the external card-subscription API.
 * Unknown fields are intentionally ignored to maintain forward compatibility.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubscribeCardApiResponse {

    private String subscriptionId;
    private String status;
    private String message;

    public boolean isSuccess() {
        return "SUCCESS".equalsIgnoreCase(status) || "SUBSCRIBED".equalsIgnoreCase(status);
    }
}
