package com.demo.cardsubscription.infrastructure.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for the external card-subscription API request body.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscribeCardApiRequest {

    private String cardId;
    private String userId;
}
