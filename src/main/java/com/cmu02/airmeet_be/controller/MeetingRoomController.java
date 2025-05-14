package com.cmu02.airmeet_be.controller;

import com.cmu02.airmeet_be.domain.dto.request.MeetingRoomRequest;
import com.cmu02.airmeet_be.domain.dto.response.MeetingRoomResponse;
import com.cmu02.airmeet_be.services.MeetingRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class MeetingRoomController {
    private final MeetingRoomService service;

    @PostMapping
    public Mono<ResponseEntity<MeetingRoomResponse>> createRoom(@RequestBody @Valid MeetingRoomRequest request) {
        return service.createRoom(request)
                .map(ResponseEntity::ok);
    }
}
