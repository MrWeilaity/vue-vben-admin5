package com.vben.framework.aspect;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vben.common.ApiResponse;
import com.vben.common.exception.ApiException;
import com.vben.system.entity.SysDept;
import com.vben.system.entity.SysOperationLog;
import com.vben.system.entity.SysUser;
import com.vben.system.mapper.SysDeptMapper;
import com.vben.system.mapper.SysUserMapper;
import com.vben.system.service.system.ISysOperationLogService;
import com.vben.framework.web.RequestIpResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 操作日志切面：统一记录增删改、导入导出等关键操作。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private static final int MAX_TEXT_LEN = 4000;

    private final ISysOperationLogService operationLogService;
    private final ObjectMapper objectMapper;
    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;
    private final RequestIpResolver requestIpResolver;

    @Around("execution(* *..controller..*.*(..))")
    public Object logOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return joinPoint.proceed();
        }
        var request = attrs.getRequest();
        String method = request.getMethod();
        String path = request.getRequestURI();
        if (!shouldLog(method, path)) {
            return joinPoint.proceed();
        }

        long begin = System.currentTimeMillis();
        Object returnValue = null;
        Throwable throwable = null;
        try {
            returnValue = joinPoint.proceed();
            return returnValue;
        } catch (Throwable ex) {
            throwable = ex;
            throw ex;
        } finally {
            long duration = System.currentTimeMillis() - begin;
            try {
                SysOperationLog logEntity = buildLog(joinPoint, request, attrs.getResponse(), method, path, duration, returnValue, throwable);
                operationLogService.saveAsync(logEntity);
            } catch (Exception e) {
                log.warn("操作日志异步入库失败: {}", e.getMessage());
            }
        }
    }

    private SysOperationLog buildLog(
            ProceedingJoinPoint joinPoint,
            jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response,
            String method,
            String path,
            long duration,
            Object returnValue,
            Throwable throwable
    ) {
        SysOperationLog item = new SysOperationLog();
        item.setOccurTime(LocalDateTime.now());
        item.setClientIp(limit(requestIpResolver.resolve(request), 64));
        item.setRequestMethod(method);
        item.setRequestUrl(limit(path, 512));
        item.setRequestParams(buildRequestParams(joinPoint.getArgs()));
        item.setDurationMs(duration);
        item.setActionType(resolveActionType(method, path));
        item.setModule(limit(resolveModule(joinPoint), 128));
        item.setOperationDesc(limit(resolveOperationDesc(joinPoint), 255));

        fillOperator(item);

        int httpStatus = resolveHttpStatus(response, throwable, returnValue);
        int bizCode = resolveBizCode(throwable, returnValue);
        item.setHttpStatusCode(httpStatus);
        item.setBizStatusCode(bizCode);
        item.setSuccess(throwable == null && httpStatus < 400 && bizCode == 0 ? 1 : 0);
        item.setErrorMessage(extractErrorMessage(throwable));

        Map<String, Object> ext = new HashMap<>();
        ext.put("operation", joinPoint.getSignature().toShortString());
        ext.put("response", summarizeReturn(returnValue));
        item.setExtData(ext);
        return item;
    }

    private boolean shouldLog(String method, String path) {
        if (path == null || path.startsWith("/api/system/log/operation")) {
            return false;
        }
        String p = path.toLowerCase(Locale.ROOT);
        return Arrays.asList("POST", "PUT", "DELETE", "PATCH").contains(method)
                || p.contains("/import")
                || p.contains("/export");
    }

    private void fillOperator(SysOperationLog item) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || "anonymousUser".equals(authentication.getName())) {
            item.setOperatorUsername("anonymous");
            return;
        }
        String username = authentication.getName();
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .last("LIMIT 1"));
        if (user != null) {
            item.setOperatorUserId(user.getId());
            item.setOperatorUsername(limit(
                    user.getNickname() == null || user.getNickname().isBlank() ? username : user.getNickname(),
                    64
            ));
            if (user.getDeptId() != null) {
                SysDept dept = deptMapper.selectById(user.getDeptId());
                if (dept != null) {
                    item.setOperatorDept(limit(dept.getName(), 128));
                }
            }
            return;
        }
        item.setOperatorUsername(limit(username, 64));
    }

    private String buildRequestParams(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        try {
            String json = objectMapper.writeValueAsString(Arrays.stream(args)
                    .filter(this::isRecordableArg)
                    .map(this::sanitizeArg)
                    .toList());
            return truncate(json);
        } catch (Exception e) {
            return "[unserializable_params]";
        }
    }

    private boolean isRecordableArg(Object arg) {
        if (arg == null) {
            return true;
        }
        return !(arg instanceof ServletRequest
                || arg instanceof ServletResponse
                || arg instanceof MultipartFile
                || arg instanceof InputStream
                || arg instanceof OutputStream
                || arg instanceof Resource
                || arg instanceof byte[]);
    }

    private Object sanitizeArg(Object arg) {
        if (arg instanceof String str && str.length() > MAX_TEXT_LEN) {
            return str.substring(0, MAX_TEXT_LEN) + "...(truncated)";
        }
        return arg;
    }

    private String summarizeReturn(Object returnValue) {
        if (returnValue == null) {
            return null;
        }
        if (returnValue instanceof byte[] || returnValue instanceof Resource || returnValue instanceof InputStream
                || returnValue instanceof OutputStream) {
            return "[stream_omitted]";
        }
        try {
            return truncate(objectMapper.writeValueAsString(returnValue));
        } catch (Exception e) {
            return "[unserializable_response]";
        }
    }

    private int resolveBizCode(Throwable throwable, Object returnValue) {
        if (throwable instanceof ApiException apiException) {
            return apiException.getCode();
        }
        if (returnValue instanceof ApiResponse<?> response) {
            return response.getCode();
        }
        return throwable == null ? 0 : 500;
    }

    private int resolveHttpStatus(jakarta.servlet.http.HttpServletResponse response, Throwable throwable, Object returnValue) {
        if (throwable instanceof ApiException apiException) {
            return apiException.getHttpStatus();
        }
        if (returnValue instanceof ResponseEntity<?> entity) {
            return entity.getStatusCode().value();
        }
        if (response != null && response.getStatus() > 0) {
            return response.getStatus();
        }
        return throwable == null ? 200 : 500;
    }

    private String resolveActionType(String method, String path) {
        String p = path == null ? "" : path.toLowerCase(Locale.ROOT);
        if (p.contains("/import")) {
            return "IMPORT";
        }
        if (p.contains("/export")) {
            return "EXPORT";
        }
        return switch (method) {
            case "POST" -> "CREATE";
            case "PUT", "PATCH" -> "UPDATE";
            case "DELETE" -> "DELETE";
            default -> "OTHER";
        };
    }

    private String resolveModule(ProceedingJoinPoint joinPoint) {
        Class<?> targetClass = AopUtils.getTargetClass(joinPoint.getTarget());
        Tag tag = AnnotationUtils.findAnnotation(targetClass, Tag.class);
        if (tag == null || tag.name() == null || tag.name().isBlank()) {
            return targetClass.getSimpleName();
        }
        return tag.name();
    }

    private String resolveOperationDesc(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Operation operation = AnnotationUtils.findAnnotation(method, Operation.class);
        if (operation != null && operation.summary() != null && !operation.summary().isBlank()) {
            return operation.summary();
        }
        Class<?> targetClass = AopUtils.getTargetClass(joinPoint.getTarget());
        try {
            method = targetClass.getMethod(signature.getName(), signature.getMethod().getParameterTypes());
        } catch (NoSuchMethodException ignored) {
        }
        operation = AnnotationUtils.findAnnotation(method, Operation.class);
        if (operation == null || operation.summary() == null || operation.summary().isBlank()) {
            return signature.toShortString();
        }
        return operation.summary();
    }

    private String extractErrorMessage(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        String message = findBusinessMessage(throwable);
        if (message != null) {
            return truncate(message);
        }
        return truncate(stackTraceOf(throwable));
    }

    private String findBusinessMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof ApiException && current.getMessage() != null && !current.getMessage().isBlank()) {
                return current.getMessage();
            }
            current = current.getCause();
        }
        current = throwable;
        while (current != null) {
            if (current.getMessage() != null && !current.getMessage().isBlank()) {
                return current.getMessage();
            }
            current = current.getCause();
        }
        return null;
    }

    private String stackTraceOf(Throwable throwable) {
        StringWriter writer = new StringWriter();
        try (PrintWriter printWriter = new PrintWriter(writer)) {
            throwable.printStackTrace(printWriter);
            printWriter.flush();
            return writer.toString();
        }
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        if (value.length() <= MAX_TEXT_LEN) {
            return value;
        }
        return value.substring(0, MAX_TEXT_LEN) + "...(truncated)";
    }

    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

}
