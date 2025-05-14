package com.cmu02.airmeet_be.controller;

import com.cmu02.airmeet_be.domain.dto.request.MeetingRoomRequest;
import com.cmu02.airmeet_be.domain.dto.response.MeetingRoomResponse;
import com.cmu02.airmeet_be.services.MeetingRoomRedisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class MeetingRoomController {
    private final MeetingRoomRedisService service;

    @PostMapping
    public Mono<ResponseEntity<MeetingRoomResponse>> createRoom(@RequestBody @Valid MeetingRoomRequest request) {
        return service.createRoom(request)
                .map(ResponseEntity::ok);
    }
}
