package com.vben.system.common.exception;

public class NotFoundException extends ApiException {
    public NotFoundException(String message) {
        super(404, 404, message);
    }
}
