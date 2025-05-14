package com.cmu02.airmeet_be.domain.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public record MeetingRoomRequest(
        @NotBlank String roomName,
        @Valid  UserRequestDto user
) {
}
