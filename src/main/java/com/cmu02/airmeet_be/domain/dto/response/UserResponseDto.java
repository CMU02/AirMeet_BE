package com.cmu02.airmeet_be.domain.dto.response;

import com.cmu02.airmeet_be.domain.model.User;
import jakarta.validation.constraints.NotBlank;

public record UserResponseDto (
        @NotBlank String uuid,
        @NotBlank String nickname
) {
    public UserResponseDto(User user) {
        this(user.getUuid(), user.getNickname());
    }
}
