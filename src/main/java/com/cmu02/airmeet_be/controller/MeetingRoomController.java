package com.cmu02.airmeet_be.controller;

import com.cmu02.airmeet_be.domain.dto.request.AddMeetingRoomRequestDto;
import com.cmu02.airmeet_be.domain.dto.request.GetMeetingRoomRequestDto;
import com.cmu02.airmeet_be.domain.dto.request.UserRequestDto;
import com.cmu02.airmeet_be.domain.dto.response.MeetingRoomResponse;
import com.cmu02.airmeet_be.domain.dto.response.MeetingRoomWithCodeResponse;
import com.cmu02.airmeet_be.services.MeetingRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class MeetingRoomController {
    private final MeetingRoomService service;

    @PostMapping
    public Mono<ResponseEntity<MeetingRoomResponse>> createRoom(@RequestBody @Valid AddMeetingRoomRequestDto request) {
        return service.createRoom(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/get-room")
    public Mono<ResponseEntity<MeetingRoomWithCodeResponse>> getRoom(
            @RequestBody @Valid GetMeetingRoomRequestDto dto
            ) {
        return service.getRoom(dto)
                .map(ResponseEntity::ok);
    }
}
