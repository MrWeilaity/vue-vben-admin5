package com.vben.system.common;

import com.vben.system.common.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Iterator;

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
                .body(ApiResponse.fail(ex.getCode(), ex.getMessage(), ex.getMessage()));
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
        String message = resolveBadRequestMessage(ex);
        log.error(
                "请求参数异常: uri={}, method={}, message={}",
                request.getRequestURI(),
                request.getMethod(),
                message,
                ex
        );
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(400, message, message));
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
                .body(ApiResponse.fail(500, "服务器内部错误", "服务器内部错误"));
    }

    private String resolveBadRequestMessage(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
            FieldError fieldError = methodArgumentNotValidException.getBindingResult().getFieldError();
            if (fieldError != null) {
                return buildFieldMessage(fieldError.getField(), fieldError.getDefaultMessage());
            }
            return "请求参数不合法";
        }
        if (ex instanceof BindException bindException) {
            FieldError fieldError = bindException.getBindingResult().getFieldError();
            if (fieldError != null) {
                return buildFieldMessage(fieldError.getField(), fieldError.getDefaultMessage());
            }
            return "请求参数不合法";
        }
        if (ex instanceof ConstraintViolationException constraintViolationException) {
            Iterator<ConstraintViolation<?>> iterator = constraintViolationException.getConstraintViolations().iterator();
            if (iterator.hasNext()) {
                ConstraintViolation<?> violation = iterator.next();
                return buildFieldMessage(extractLeafPath(violation.getPropertyPath() == null ? null : violation.getPropertyPath().toString()), violation.getMessage());
            }
            return "请求参数不合法";
        }
        if (ex instanceof MethodArgumentTypeMismatchException typeMismatchException) {
            return buildFieldMessage(typeMismatchException.getName(), "参数类型错误");
        }
        if (ex instanceof IllegalArgumentException && ex.getMessage() != null && !ex.getMessage().isBlank()) {
            return ex.getMessage();
        }
        return "请求参数不合法";
    }

    private String buildFieldMessage(String field, String message) {
        if (message == null || message.isBlank()) {
            return field == null || field.isBlank() ? "请求参数不合法" : field + "参数不合法";
        }
        if (field == null || field.isBlank()) {
            return message;
        }
        return field + message;
    }

    private String extractLeafPath(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        int index = path.lastIndexOf('.');
        return index >= 0 ? path.substring(index + 1) : path;
    }
}
