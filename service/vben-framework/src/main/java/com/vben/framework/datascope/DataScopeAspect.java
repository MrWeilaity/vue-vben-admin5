package com.vben.framework.datascope;

import com.vben.system.datascope.DataScope;
import com.vben.system.datascope.DataScopeService;
import com.vben.system.datascope.DataScopes;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 为标记了 @DataScope 或 @DataScopes 的查询方法准备数据权限上下文。
 */
@Aspect
@Component
@RequiredArgsConstructor
public class DataScopeAspect {
    private final DataScopeService dataScopeService;

    /**
     * 包裹带 {@link DataScope} 注解的查询方法。
     * <p>
     * 方法执行前计算当前用户的数据权限并放入线程上下文；方法结束后清理上下文，
     * 避免同一请求线程后续查询误用本次数据权限规则。
     */
    @Around("@annotation(dataScope)")
    public Object around(ProceedingJoinPoint joinPoint, DataScope dataScope) throws Throwable {
        return runWithDataScope(joinPoint, List.of(dataScope));
    }

    /**
     * 包裹带 {@link DataScopes} 注解的查询方法。
     * <p>
     * 多个规则共享同一份当前登录用户权限结果，拦截器再按当前 Mapper 匹配具体字段配置。
     */
    @Around("@annotation(dataScopes)")
    public Object aroundDataScopes(ProceedingJoinPoint joinPoint, DataScopes dataScopes) throws Throwable {
        return runWithDataScope(joinPoint, Arrays.asList(dataScopes.value()));
    }

    private Object runWithDataScope(ProceedingJoinPoint joinPoint, List<DataScope> scopes) throws Throwable {
        DataScopeContext.set(scopes, dataScopeService.resolveCurrentRule());
        try {
            return joinPoint.proceed();
        } finally {
            DataScopeContext.clear();
        }
    }
}
