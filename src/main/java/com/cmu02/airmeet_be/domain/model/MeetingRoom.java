package com.cmu02.airmeet_be.domain.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetingRoom {
    private String roomId;
    private String roomName;
    private String host;
    private String joinCode;
    private LocalDateTime createdDate;
}
