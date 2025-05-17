package com.cmu02.airmeet_be.services;

import com.cmu02.airmeet_be.domain.dto.request.AddUserRequestDto;
import com.cmu02.airmeet_be.domain.dto.request.JoinRoomRequestDto;
import com.cmu02.airmeet_be.domain.dto.request.UserRequestDto;
import com.cmu02.airmeet_be.domain.dto.response.MeetingRoomResponse;
import com.cmu02.airmeet_be.domain.dto.response.UserResponseDto;
import com.cmu02.airmeet_be.domain.model.MeetingRoom;
import com.cmu02.airmeet_be.domain.model.User;
import com.cmu02.airmeet_be.utils.Key;
import com.cmu02.airmeet_be.utils.KeyPreFix;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final ReactiveRedisTemplate<String, User> userRedisTemplate; // 사용자고유아이디/사용자 정보 템플릿
    private final ReactiveRedisTemplate<String, MeetingRoom> roomRedisTemplate; // 회의방 아이디/회의방정보 템플릿
    private final ReactiveRedisTemplate<String, String> defaultRedisTemplate;
    private final Key key;

    // 유저 추가
    public Mono<UserResponseDto> addUser(AddUserRequestDto request) {
        String userId = UUID.randomUUID().toString();

        User userProfile = User.builder()
                .uuid(userId)
                .nickname(request.nickname())
                .build();

        return Mono.when(
                userRedisTemplate.opsForValue().set(KeyPreFix.USER_KEY_PREFIX.getKeyPrefix() + userId, userProfile)
        ).thenReturn(new UserResponseDto(userProfile));
    }

    // JoinCode로 이용하여 회의방 참가
    public Mono<MeetingRoomResponse> joinRoomByCode(JoinRoomRequestDto request) {
        String userId = request.uuid();
        String joinCode = request.code();

        return userRedisTemplate.opsForValue().get(key.getUserKey(userId))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("해당 사용자는 없습니다.")))
                .flatMap(user ->
                    defaultRedisTemplate.opsForValue().get(key.getCodeKey(joinCode))
                            .switchIfEmpty(Mono.error(new IllegalArgumentException("유효하지 않는 코드입니다.")))
                            .flatMap(roomId -> {
                                return roomRedisTemplate.opsForValue().get(key.getRoomKey(roomId))
                                        .switchIfEmpty(Mono.error(new IllegalArgumentException("회의방이 존재하지 않습니다.")))
                                        .flatMap(room -> Mono.when(
                                                // 참가자 목록 추가
                                                defaultRedisTemplate.opsForSet().add(key.enterUserListKey(roomId), userId),
                                                // 사용자 참가한 방 추가
                                                defaultRedisTemplate.opsForSet().add(key.enterUserRoomKey(userId), roomId)
                                        ).thenReturn(new MeetingRoomResponse(room)));
                            })
                );
    }

    // 해당 유저가 들어가 있는 모든 회의방 조회
    public Flux<MeetingRoomResponse> getRoomsByUser(UserRequestDto request) {
        return defaultRedisTemplate.opsForSet().members(key.enterUserRoomKey(request.uuid()))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("해당 사용자는 존재하지 않거나 참여 중인 회의방이 아닙니다.")))
                .flatMap(roomId -> roomRedisTemplate.opsForValue()
                            .get(key.getRoomKey(roomId))
                            .switchIfEmpty(
                                    Mono.error(new IllegalArgumentException("존재하지 않는 회의방 입니다."))
                            ).map(MeetingRoomResponse::new)
                );
    }
}
