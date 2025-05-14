package com.cmu02.airmeet_be.services;

import com.cmu02.airmeet_be.domain.dto.request.JoinRoomRequestDto;
import com.cmu02.airmeet_be.domain.dto.request.UserRequestDto;
import com.cmu02.airmeet_be.domain.dto.response.MeetingRoomResponse;
import com.cmu02.airmeet_be.domain.dto.response.UserResponseDto;
import com.cmu02.airmeet_be.domain.model.MeetingRoom;
import com.cmu02.airmeet_be.domain.model.User;
import com.cmu02.airmeet_be.utils.KeyPreFix;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final ReactiveRedisTemplate<String, User> userRedisTemplate; // 사용자고유아이디/사용자 정보 템플릿
    private final ReactiveRedisTemplate<String, MeetingRoom> roomRedisTemplate; // 회의방 아이디/회의방정보 템플릿
    private final ReactiveRedisTemplate<String, String> defaultRedisTemplate;


    public Mono<UserResponseDto> addUser(UserRequestDto request) {
        String userId = UUID.randomUUID().toString();

        User userProfile = User.builder()
                .uuid(userId)
                .nickname(request.nickname())
                .build();

        return Mono.when(
                userRedisTemplate.opsForValue().set(KeyPreFix.USER_KEY_PREFIX.getKeyPrefix() + userId, userProfile)
        ).thenReturn(new UserResponseDto(userProfile));
    }

    public Mono<MeetingRoomResponse> joinRoomByCode(JoinRoomRequestDto request) {
        String userId = request.uuid();
        String joinCode = request.code();


        String userKey = KeyPreFix.USER_KEY_PREFIX.getKeyPrefix() + userId;
        String codeKey = KeyPreFix.CODE_KEY_PREFIX.getKeyPrefix() + joinCode;

        return userRedisTemplate.opsForValue().get(userKey)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("해당 사용자는 없습니다.")))
                .flatMap(user ->
                    defaultRedisTemplate.opsForValue().get(codeKey)
                            .switchIfEmpty(Mono.error(new IllegalArgumentException("유효하지 않는 코드입니다.")))
                            .flatMap(roomId -> {
                                String roomKey = KeyPreFix.ROOM_KEY_PREFIX.getKeyPrefix() + roomId; // 방키
                                String roomUsersKey = roomKey + ":users"; // 참가자 목록
                                String userRoomsKey = KeyPreFix.USER_KEY_PREFIX.getKeyPrefix() + userId + ":rooms"; // 사용자가 참가한 방

                                return roomRedisTemplate.opsForValue().get(roomKey)
                                        .switchIfEmpty(Mono.error(new IllegalArgumentException("회의방이 존재하지 않습니다.")))
                                        .flatMap(room -> Mono.when(
                                                defaultRedisTemplate.opsForSet().add(roomUsersKey, userId),
                                                defaultRedisTemplate.opsForSet().add(userRoomsKey, roomId)
                                        ).thenReturn(new MeetingRoomResponse(room)));
                            })
                );
    }
}
