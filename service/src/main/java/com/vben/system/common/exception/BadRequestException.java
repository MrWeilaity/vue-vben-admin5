package com.vben.system.common.exception;

public class BadRequestException extends ApiException {
    public BadRequestException(String message) {
        super(400, 400, message);
    }
}
