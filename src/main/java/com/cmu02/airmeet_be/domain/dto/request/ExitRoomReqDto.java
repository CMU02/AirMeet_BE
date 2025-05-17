package com.cmu02.airmeet_be.domain.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ExitRoomReqDto(
        @NotBlank String uuid,
        @NotBlank String roomId
) {
}
