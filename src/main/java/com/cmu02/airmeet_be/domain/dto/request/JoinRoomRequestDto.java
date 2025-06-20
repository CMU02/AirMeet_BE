package com.cmu02.airmeet_be.domain.dto.request;

import jakarta.validation.constraints.NotBlank;

public record JoinRoomRequestDto(
        @NotBlank String code,
        @NotBlank String uuid
) {
}
