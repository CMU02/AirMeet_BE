package com.cmu02.airmeet_be.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    NOT_FOUND_USER_ID(HttpStatus.NOT_FOUND.value(), "해당 사용자는 없습니다."),
    NOT_FOUND_ROOM(HttpStatus.NOT_FOUND.value(), "존재하지 않는 회의방 입니다."),
    NOT_VALID_JOIN_CODE(HttpStatus.NOT_FOUND.value(), "유효하지 않는 코드 입니다.");

    private final int code;
    private final String message;
}
