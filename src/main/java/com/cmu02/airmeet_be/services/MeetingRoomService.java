package com.cmu02.airmeet_be.services;

import com.cmu02.airmeet_be.domain.dto.request.MeetingRoomRequest;
import com.cmu02.airmeet_be.domain.dto.response.MeetingRoomResponse;
import com.cmu02.airmeet_be.domain.model.MeetingRoom;
import com.cmu02.airmeet_be.domain.model.User;
import com.cmu02.airmeet_be.utils.KeyPreFix;
import com.cmu02.airmeet_be.utils.KeySuffix;
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
    private final RandomCode randomCode;
    private final ReactiveRedisTemplate<String, User> userRedisTemplate;

    // 회의방 생성
    public Mono<MeetingRoomResponse> createRoom(MeetingRoomRequest request) {
        String roomId = UUID.randomUUID().toString(); // 방 ID
        String userKey = KeyPreFix.USER_KEY_PREFIX.getKeyPrefix() + request.user().uuid(); // EX: user:232jis-...

        return userRedisTemplate.opsForValue().get(userKey)
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

                            // 참가자 목록
                            String roomUserKey = KeyPreFix.ROOM_KEY_PREFIX.getKeyPrefix()
                                    + roomId
                                    + KeySuffix.USERS_SUFFIX.getKeySuffix();
                            // 사용자가 참가한 방
                            String userRoomsKey = KeyPreFix.USER_KEY_PREFIX.getKeyPrefix()
                                    + user.getUuid()
                                    + KeySuffix.ROOMS_SUFFIX.getKeySuffix();

                            return Mono.when(
                                    // 생성한 회의방ID/회의방 정보 저장
                                    roomRedisTemplate.opsForValue().set(KeyPreFix.ROOM_KEY_PREFIX.getKeyPrefix() + roomId, room),
                                    // 생성한 조인코드/회의방ID 저장
                                    defaultRedisTemplate.opsForValue().set(KeyPreFix.CODE_KEY_PREFIX.getKeyPrefix() + joinCode, roomId),
                                    // 생성한 회의방 참가 및 참가자 목록
                                    defaultRedisTemplate.opsForSet().add(roomUserKey, user.getUuid()),
                                    // 해당 사용자가 참가한 방
                                    defaultRedisTemplate.opsForSet().add(userRoomsKey, roomId)
                            ).thenReturn(new MeetingRoomResponse(room));
                        })
                );
    }
}
