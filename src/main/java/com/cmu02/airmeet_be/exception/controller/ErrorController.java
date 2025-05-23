package com.cmu02.airmeet_be.exception.controller;

import com.cmu02.airmeet_be.exception.BaseErrorResponse;
import com.cmu02.airmeet_be.exception.BaseException;
import com.cmu02.airmeet_be.exception.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class ErrorController {

    @ExceptionHandler
    public ResponseEntity<BaseErrorResponse> handleBaseException(BaseException e) {
        ErrorCode errorCode = e.getErrorCode();

        BaseErrorResponse response = BaseErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(errorCode.getCode()).body(response);
    }
}
