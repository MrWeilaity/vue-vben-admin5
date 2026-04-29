package com.vben.common.exception;

public class ForbiddenException extends ApiException {
    public ForbiddenException(String message) {
        super(403, 403, message);
    }
}
