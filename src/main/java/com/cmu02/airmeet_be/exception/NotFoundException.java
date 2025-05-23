package com.cmu02.airmeet_be.exception;

public class NotFoundException extends BaseException {

    private final ErrorCode errorCode;

    public NotFoundException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    @Override
    public ErrorCode getErrorCode() {
        return this.errorCode;
    }
}
