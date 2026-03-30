package com.vben.system.common;

import com.vben.system.common.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(
        ApiException ex,
        HttpServletRequest request
    ) {
        log.error(
            "API业务异常: uri={}, method={}, code={}, httpStatus={}, message={}",
            request.getRequestURI(),
            request.getMethod(),
            ex.getCode(),
            ex.getHttpStatus(),
            ex.getMessage(),
            ex
        );
        return ResponseEntity
            .status(ex.getHttpStatus())
            .body(ApiResponse.fail(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler({
        BindException.class,
        ConstraintViolationException.class,
        IllegalArgumentException.class,
        MethodArgumentNotValidException.class,
        MethodArgumentTypeMismatchException.class,
    })
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(
        Exception ex,
        HttpServletRequest request
    ) {
        log.error(
            "请求参数异常: uri={}, method={}, message={}",
            request.getRequestURI(),
            request.getMethod(),
            ex.getMessage(),
            ex
        );
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.fail(400, "请求参数错误"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(
        Exception ex,
        HttpServletRequest request
    ) {
        log.error(
            "未处理异常: uri={}, method={}, message={}",
            request.getRequestURI(),
            request.getMethod(),
            ex.getMessage(),
            ex
        );
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.fail(500, ex.getMessage()));
    }
}
