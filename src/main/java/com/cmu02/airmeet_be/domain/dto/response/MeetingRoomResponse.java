package com.cmu02.airmeet_be.domain.dto.response;

import com.cmu02.airmeet_be.domain.model.MeetingRoom;

import java.time.LocalDateTime;

public record MeetingRoomResponse(
        String roomId,
        String roomName,
        String host,
        LocalDateTime createdDate
) {
    public MeetingRoomResponse(MeetingRoom room) {
        this(room.getRoomId(), room.getRoomName(), room.getHost(), room.getCreatedDate());
    }
}
