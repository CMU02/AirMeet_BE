package com.cmu02.airmeet_be.controller;

import com.cmu02.airmeet_be.domain.dto.request.AddUserRequestDto;
import com.cmu02.airmeet_be.domain.dto.request.ExitRoomRequestDto;
import com.cmu02.airmeet_be.domain.dto.request.JoinRoomRequestDto;
import com.cmu02.airmeet_be.domain.dto.request.UserRequestDto;
import com.cmu02.airmeet_be.domain.dto.response.MeetingRoomResponse;
import com.cmu02.airmeet_be.domain.dto.response.UserResponseDto;
import com.cmu02.airmeet_be.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService service;

    // 유저 생성
    @PostMapping("/add")
    public Mono<ResponseEntity<UserResponseDto>> addUser(@RequestBody AddUserRequestDto dto) {
        return service.addUser(dto)
                .map(ResponseEntity::ok);
    }

    // 조인코드를 이용하여 회의방 참가
    @PostMapping("/joinRoom")
    public Mono<ResponseEntity<MeetingRoomResponse>> joinRoom(@RequestBody @Valid JoinRoomRequestDto dto) {
        return service.joinRoomByCode(dto)
                .map(ResponseEntity::ok);
    }

    // 조인코드를 이용하여 회의방 정보 가져오기
    @GetMapping("/get-joinRoom")
    public Mono<ResponseEntity<MeetingRoomResponse>> getRoomByCode(@RequestBody @Valid JoinRoomRequestDto dto) {
        return service.getRoomByCode(dto)
                .map(ResponseEntity::ok);
    }

    // 해당 유저가 참가하고 있는 방 조회
    @GetMapping("/rooms")
    public Flux<MeetingRoomResponse> getRoomsByUser(@RequestBody UserRequestDto dto) {
        return service.getRoomsByUser(dto);
    }

    // 해당 회의방 퇴장
    @PostMapping("/exit-rooms")
    public Mono<Void> removeUserFromRoom(@RequestBody @Valid ExitRoomRequestDto dto) {
        return service.removeUserFromRoom(dto);
    }
}
