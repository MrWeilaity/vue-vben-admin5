package com.vben.system.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * OperationLogAspect 组件说明。
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    @AfterReturning("execution(* com.vben.system.controller..*(..))")
    public void logOperation(JoinPoint joinPoint) {
        log.info("op={} args={}", joinPoint.getSignature().toShortString(), joinPoint.getArgs().length);
    }
}
