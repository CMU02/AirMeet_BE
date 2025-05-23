package com.cmu02.airmeet_be.exception;

public abstract class BaseException extends RuntimeException {
    public BaseException(String message) {
        super(message);
    }

    public abstract ErrorCode getErrorCode();
}
