package com.cmu02.airmeet_be.domain.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record ChatMessage(
        @NotBlank String roomId,
        @NotBlank String senderUuid,
        @NotBlank String senderNickname,
        String content,
        LocalDateTime timestamp
) {
}
