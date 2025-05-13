package com.cmu02.airmeet_be.services;

import com.cmu02.airmeet_be.domain.dto.request.MeetingRoomRequest;
import com.cmu02.airmeet_be.domain.dto.response.MeetingRoomResponse;
import com.cmu02.airmeet_be.domain.model.MeetingRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MeetingRoomRedisService {
    private final ReactiveRedisTemplate<String, MeetingRoom> redisTemplate;

    public Mono<MeetingRoomResponse> createRoom(MeetingRoomRequest request) {
        String roomId = UUID.randomUUID().toString(); // 방 ID

        MeetingRoom room = MeetingRoom.builder()
                .roomId(roomId)
                .host(request.host())
                .topic(request.topic())
                .roomName(request.roomName())
                .createdDate(LocalDateTime.now())
                .build();

        return redisTemplate.opsForValue()
                .set(request.roomName(), room)
                .thenReturn(new MeetingRoomResponse(room));
    }

    public Mono<MeetingRoomResponse> getRoom(String roomName) {
        return redisTemplate.opsForValue()
                .get(roomName)
                .map(MeetingRoomResponse::new);
    }
}
