package com.cmu02.airmeet_be.domain.dto.response;

import com.cmu02.airmeet_be.domain.model.MeetingRoom;

import java.time.LocalDateTime;

public record MeetingRoomWithCodeResponse(
        String roomId,
        String roomName,
        String joinCode,
        String host,
        LocalDateTime createdData
) {
    public MeetingRoomWithCodeResponse(MeetingRoom room) {
        this(room.getRoomId(), room.getRoomName(), room.getJoinCode(), room.getHost(), room.getCreatedDate());
    }
}
