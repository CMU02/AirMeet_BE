package com.cmu02.airmeet_be.services;

import com.cmu02.airmeet_be.domain.dto.request.MeetingRoomRequest;
import com.cmu02.airmeet_be.domain.dto.response.MeetingRoomResponse;
import com.cmu02.airmeet_be.domain.model.MeetingRoom;
import com.cmu02.airmeet_be.domain.model.User;
import com.cmu02.airmeet_be.utils.Key;
import com.cmu02.airmeet_be.utils.RandomCode;
import lombok.RequiredArgsConstructor;
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
    private final ReactiveRedisTemplate<String, User> userRedisTemplate;
    private final RandomCode randomCode;
    private final Key key;

    // 회의방 생성
    public Mono<MeetingRoomResponse> createRoom(MeetingRoomRequest request) {
        String roomId = UUID.randomUUID().toString(); // 방 ID
        String userid = request.user().uuid();

        return userRedisTemplate.opsForValue().get(key.getUserKey(userid))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("해당 사용자가 존재하지 않습니다.")))
                .flatMap(user ->
                        randomCode.generateJoinCode().flatMap(joinCode -> {
                            // 1. 회의방 객체 생성
                            MeetingRoom room = MeetingRoom.builder()
                                    .roomId(roomId)
                                    .host(user.getNickname())
                                    .roomName(request.roomName())
                                    .joinCode(joinCode)
                                    .createdDate(LocalDateTime.now())
                                    .build();

                            return Mono.when(
                                    // 생성한 회의방ID/회의방 정보 추가
                                    roomRedisTemplate.opsForValue().set(key.getRoomKey(roomId), room),
                                    // 생성한 조인코드/회의방ID 추가
                                    defaultRedisTemplate.opsForValue().set(key.getCodeKey(joinCode), roomId),
                                    // 생성한 회의방 참가 및 참가자 목록
                                    defaultRedisTemplate.opsForSet().add(key.enterUserListKey(roomId), user.getUuid()),
                                    // 해당 사용자가 참가한 방
                                    defaultRedisTemplate.opsForSet().add(key.enterUserRoomKey(userid), roomId)
                            ).thenReturn(new MeetingRoomResponse(room));
                        })
                );
    }
}
