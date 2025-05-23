package com.cmu02.airmeet_be.exception;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record BaseErrorResponse(
        int code,
        String message,
        LocalDateTime timestamp
) {
}
