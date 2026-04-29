package com.vben.common.exception;

public class RefreshTokenExpiredException extends ApiException {
    public static final int CODE = 498;
    public static final int HTTP_STATUS = 498;

    public RefreshTokenExpiredException(String message) {
        super(CODE, HTTP_STATUS, message);
    }
}
