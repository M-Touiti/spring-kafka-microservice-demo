package com.demo.cardsubscription.exposition.exception;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Structured error response returned by the global exception handler.
 */
public record ApiError(
        int status,
        String error,
        String message,
        LocalDateTime timestamp,
        List<String> details
) {
    public static ApiError of(int status, String error, String message) {
        return new ApiError(status, error, message, LocalDateTime.now(), List.of());
    }

    public static ApiError of(int status, String error, String message, List<String> details) {
        return new ApiError(status, error, message, LocalDateTime.now(), details);
    }
}
