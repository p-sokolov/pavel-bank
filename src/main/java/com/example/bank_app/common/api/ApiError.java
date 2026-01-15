package com.example.bank_app.common.api;

import java.time.Instant;

public record ApiError(
        String code,
        String message,
        String path,
        Instant timestamp
) {
    public static ApiError of(String code, String message, String path) {
        return new ApiError(code, message, path, Instant.now());
    }
}
