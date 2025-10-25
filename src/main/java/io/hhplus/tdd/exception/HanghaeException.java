package io.hhplus.tdd.exception;

import lombok.Getter;

@Getter
public class HanghaeException extends RuntimeException {

    private final ErrorCode errorCode;

    public HanghaeException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public String getErrorCodeValue() {
        return errorCode.getCode();
    }
}