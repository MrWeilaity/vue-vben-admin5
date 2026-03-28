package com.vben.system.aspect;

import com.vben.system.dto.system.log.OperationLogRecord;
import com.vben.system.service.system.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * OperationLogAspect 组件说明。
 */
@Slf4j
@Aspect
@Component
@lombok.RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogService operationLogService;

    @Around("execution(* com.vben.system.controller..*(..))")
    public Object logOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        long begin = System.currentTimeMillis();
        String result = "SUCCESS";
        String error = null;
        try {
            return joinPoint.proceed();
        } catch (Throwable ex) {
            result = "FAILED";
            error = ex.getMessage();
            throw ex;
        } finally {
            long duration = System.currentTimeMillis() - begin;
            OperationLogRecord record = buildRecord(joinPoint, duration, result, error);
            operationLogService.save(record);
            log.info("op={} result={} duration={}ms", joinPoint.getSignature().toShortString(), result, duration);
        }
    }

    private OperationLogRecord buildRecord(JoinPoint joinPoint, long duration, String result, String error) {
        String username = "anonymous";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() != null) {
            username = authentication.getName();
        }

        String path = null;
        String method = null;
        String ip = null;
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            var request = attributes.getRequest();
            path = request.getRequestURI();
            method = request.getMethod();
            ip = request.getRemoteAddr();
        }

        return OperationLogRecord.builder()
            .username(username)
            .ip(ip)
            .method(method)
            .path(path)
            .operation(joinPoint.getSignature().toShortString())
            .result(result)
            .error(error)
            .durationMs(duration)
            .time(LocalDateTime.now())
            .build();
    }
}
