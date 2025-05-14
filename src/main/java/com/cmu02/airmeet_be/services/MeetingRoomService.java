package com.cmu02.airmeet_be.services;

import com.cmu02.airmeet_be.domain.dto.request.MeetingRoomRequest;
import com.cmu02.airmeet_be.domain.dto.response.MeetingRoomResponse;
import com.cmu02.airmeet_be.domain.model.MeetingRoom;
import com.cmu02.airmeet_be.domain.model.User;
import com.cmu02.airmeet_be.utils.KeyPreFix;
import com.cmu02.airmeet_be.utils.RandomCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MeetingRoomService {
    private final ReactiveRedisTemplate<String, MeetingRoom> roomRedisTemplate; // 회의방 아이디/회의방정보 템플릿
    private final ReactiveRedisTemplate<String, String> defaultRedisTemplate;
    private final RandomCode randomCode;

    // 회의방 생성
    // @TODO host가 방을 생성한 다음 해당 방에 들어가는 로직 추가
    public Mono<MeetingRoomResponse> createRoom(MeetingRoomRequest request) {
        String roomId = UUID.randomUUID().toString(); // 방 ID

        return randomCode.generateJoinCode().flatMap(joinCode -> {
            // 1. 회의방 객체 생성
            MeetingRoom room = MeetingRoom.builder()
                    .roomId(roomId)
                    .host(request.user().nickname())
                    .roomName(request.roomName())
                    .joinCode(joinCode)
                    .createdDate(LocalDateTime.now())
                    .build();

            return Mono.when(
                    roomRedisTemplate.opsForValue().set(KeyPreFix.ROOM_KEY_PREFIX.getKeyPrefix() + roomId, room),
                    defaultRedisTemplate.opsForValue().set(KeyPreFix.CODE_KEY_PREFIX.getKeyPrefix() + joinCode, roomId)
            ).thenReturn(new MeetingRoomResponse(room));
        });
    }
}
