package com.vben.common.exception;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private final int code;

    private final int httpStatus;

    public ApiException(int code, int httpStatus, String message) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }
}
