package com.cmu02.airmeet_be.services;

import com.cmu02.airmeet_be.domain.dto.request.AddUserRequestDto;
import com.cmu02.airmeet_be.domain.dto.request.ExitRoomRequestDto;
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

    // JoinCode를 이용하여 회의방 정보 가져오기
    public Mono<MeetingRoomResponse> getRoomByCode(JoinRoomRequestDto request) {
        String userId = request.uuid();
        String joinCode = request.code();

        return userRedisTemplate.opsForValue().get(key.getUserKey(userId)) // 사용자 UUID 유무
                .switchIfEmpty(Mono.error(new IllegalArgumentException("해당 사용자는 없습니다.")))
                .flatMap(user ->
                        defaultRedisTemplate.opsForValue().get(key.getCodeKey(joinCode)) // 참가코드 유무
                                .switchIfEmpty(Mono.error(new IllegalArgumentException("유효하지 않는 코드 입니다.")))
                                .flatMap(roomId ->
                                        roomRedisTemplate.opsForValue().get(key.getRoomKey(roomId)) // 회의방 존재 유무
                                                .switchIfEmpty(Mono.error(new IllegalArgumentException("회의방이 존재하지 않습니다.")))
                                        .map(MeetingRoomResponse::new)
                                )
                );
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
                            .flatMap(roomId ->
                                roomRedisTemplate.opsForValue().get(key.getRoomKey(roomId))
                                        .switchIfEmpty(Mono.error(new IllegalArgumentException("회의방이 존재하지 않습니다.")))
                                        .flatMap(room -> Mono.when(
                                                // 참가자 목록 추가
                                                defaultRedisTemplate.opsForSet().add(key.enterUserListKey(roomId), userId),
                                                // 사용자 참가한 방 추가
                                                defaultRedisTemplate.opsForSet().add(key.enterUserRoomKey(userId), roomId)
                                        ).thenReturn(new MeetingRoomResponse(room)))
                            )
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

    // 해당 방 퇴장
    public Mono<Void> removeUserFromRoom(ExitRoomRequestDto request) {
        /**
         * 시나리오
         * 1. 사용자 존재 여부 확인
         * - 1-1. 잘못된 UUID가 오면 여기서 걸러낸다.
         * - 1-2. 데이터가 비어있으면 예외를 터뜨려 파이프라인을 종료시킨다.
         * 2. 회의방 존재 여부 확인
         * - 2-1. 사용자와 마찬가지로 잘못된 회의방 ID가 오면 걸러낸다.
         * - 2-2. 데이터가 비어있으면 위와 같이 예외 터뜨려 파이프라인을 종료시킨다.
         * 3. 실제 퇴장 처리
         * - user:uuid:rooms에서 roomId 삭제
         * - room:roomId:users에서 uuid 삭제
         */
        return userRedisTemplate.opsForValue().get(key.getUserKey(request.uuid()))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("해당 사용자는 없습니다.")))
                .flatMap(user -> roomRedisTemplate.opsForValue()
                        .get(key.getRoomKey(request.roomId()))
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("존재하지 않는 회의방입니다.")))
                        .flatMap(meetingRoom -> Mono.when(
                               defaultRedisTemplate.opsForSet().remove(key.enterUserRoomKey(user.getUuid()), meetingRoom.getRoomId()),
                               defaultRedisTemplate.opsForSet().remove(key.enterUserListKey(meetingRoom.getRoomId()), user.getUuid())
                        ))
                );
    }
}
