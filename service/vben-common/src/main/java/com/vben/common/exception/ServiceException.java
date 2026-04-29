package com.vben.common.exception;

public class ServiceException extends ApiException {
    public ServiceException(String message) {
        super(1001, 400, message);
    }
}
