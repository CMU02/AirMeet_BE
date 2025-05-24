package com.cmu02.airmeet_be.domain.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public record GetMeetingRoomRequestDto(
        @NotBlank String roomId,
        @Valid UserRequestDto user
) {
}
