package com.cmu02.airmeet_be.domain.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MeetingRoomRequest(
        @NotBlank String host,
        @NotBlank String roomName
) {
}
