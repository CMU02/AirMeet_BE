package com.cmu02.airmeet_be.controller;

import com.cmu02.airmeet_be.domain.dto.request.JoinRoomRequestDto;
import com.cmu02.airmeet_be.domain.dto.request.UserRequestDto;
import com.cmu02.airmeet_be.domain.dto.response.MeetingRoomResponse;
import com.cmu02.airmeet_be.domain.dto.response.UserResponseDto;
import com.cmu02.airmeet_be.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService service;

    @PostMapping("/add")
    public Mono<ResponseEntity<UserResponseDto>> addUser(@RequestBody UserRequestDto dto) {
        return service.addUser(dto)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/joinRoom")
    public Mono<ResponseEntity<MeetingRoomResponse>> joinRoom(@RequestBody @Valid JoinRoomRequestDto dto) {
        return service.joinRoomByCode(dto)
                .map(ResponseEntity::ok);
    }
}
