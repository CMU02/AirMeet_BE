package com.cmu02.airmeet_be.domain.dto.response;

import com.cmu02.airmeet_be.domain.model.MeetingRoom;

import java.time.LocalDateTime;

public record MeetingRoomResponse(
        String roomId,
        String host,
        String topic,
        LocalDateTime createdDate
) {
    public MeetingRoomResponse(MeetingRoom room) {
        this(room.getRoomId(), room.getHost(), room.getTopic(), room.getCreatedDate());
    }
}
