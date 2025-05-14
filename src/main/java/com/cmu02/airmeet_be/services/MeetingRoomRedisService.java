package com.cmu02.airmeet_be.services;

import com.cmu02.airmeet_be.domain.dto.request.MeetingRoomRequest;
import com.cmu02.airmeet_be.domain.dto.response.MeetingRoomResponse;
import com.cmu02.airmeet_be.domain.model.MeetingRoom;
import com.cmu02.airmeet_be.utils.RandomCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.data.crossstore.ChangeSetPersister.*;

@Service
@RequiredArgsConstructor
public class MeetingRoomRedisService {
    // Key : RoomId(UUID), Value : MeetingRoom(Object)
    private final ReactiveRedisTemplate<String, MeetingRoom> roomRedisTemplate;
    private final ReactiveRedisTemplate<String, String> joinCodeRedisTemplate;
    private final RandomCode randomCode;

    // 회의방 생성
    public Mono<MeetingRoomResponse> createRoom(MeetingRoomRequest request) {
        String roomId = UUID.randomUUID().toString(); // 방 ID
        Mono<String> code = randomCode.generateJoinCode();

        return code.flatMap(joinCode -> {
            MeetingRoom room = MeetingRoom.builder()
                    .roomId(roomId)
                    .host(request.host())
                    .roomName(request.roomName())
                    .joinCode(joinCode)
                    .createdDate(LocalDateTime.now())
                    .build();

            return Mono.when(
                    roomRedisTemplate.opsForValue().set("room:" + roomId, room),
                    joinCodeRedisTemplate.opsForValue().set("code:" + joinCode, roomId)
            ).thenReturn(new MeetingRoomResponse(room));
        });
    }

    // 4자리 코드로 입장
//    public Mono<MeetingRoomResponse> joinRoomByCode(String joinCode) {
//        return joinCodeRedisTemplate.opsForValue()
//                .get(joinCode)
//                .switchIfEmpty(Mono.error(new IllegalArgumentException("Invalid join code: " + joinCode)))
//                .flatMap(roomId ->
//                        roomRedisTemplate.opsForValue().get(roomId)
//                                .switchIfEmpty(Mono.error(new IllegalArgumentException("Invalid room id: " + roomId)))
//                ).map(MeetingRoomResponse::new);
//    }
}
