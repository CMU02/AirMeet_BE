package com.cmu02.airmeet_be.utils;

import lombok.Getter;
import org.springframework.stereotype.Component;


@Getter
@Component
public class Key {

    /**
     * 사용작 정보 키
     * @param uuid
     * @return "user:uuid"
     */
    public String getUserKey(String uuid) {
        return KeyPreFix.USER_KEY_PREFIX.getKeyPrefix() + uuid;
    }

    /**
     * 회의방 정보 키
     * @param roomId
     * @return "room:roomId"
     */
    public String getRoomKey(String roomId) {
        return KeyPreFix.ROOM_KEY_PREFIX.getKeyPrefix() + roomId;
    }

    /**
     * 생성한 회의방 조인코드 키
     * @param joinCode
     * @return "code:joinCode"
     */
    public String getCodeKey(String joinCode) {
        return KeyPreFix.CODE_KEY_PREFIX.getKeyPrefix() + joinCode;
    }

    /**
     * 참가자 목록 키
     * @param roomId
     * @return "room:roomId:users"
     */
    public String enterUserListKey(String roomId) {
        return KeyPreFix.ROOM_KEY_PREFIX.getKeyPrefix() + roomId + KeySuffix.USERS_SUFFIX.getKeySuffix();
    }

    /**
     * 사용자가 참가한 방 키
     * @param uuid
     * @return "user:uuid:rooms"
     */
    public String enterUserRoomKey(String uuid) {
        return KeyPreFix.USER_KEY_PREFIX.getKeyPrefix() + uuid + KeySuffix.ROOMS_SUFFIX.getKeySuffix();
    }
}
