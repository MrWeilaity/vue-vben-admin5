package com.vben.system.security.datascope;

import java.util.List;

/**
 * 当前线程数据权限上下文。
 */
public final class DataScopeContext {
    private static final ThreadLocal<Holder> CONTEXT = new ThreadLocal<>();

    private DataScopeContext() {
    }

    /**
     * 保存当前查询方法的数据权限规则和计算后的权限结果。
     */
    public static void set(List<DataScope> scopes, DataScopeRule rule) {
        CONTEXT.set(new Holder(scopes, rule));
    }

    /**
     * 获取当前线程中的数据权限上下文。
     */
    public static Holder get() {
        return CONTEXT.get();
    }

    /**
     * 清理当前线程中的数据权限上下文。
     */
    public static void clear() {
        CONTEXT.remove();
    }

    /**
     * 数据权限上下文内容。
     *
     * @param scopes 查询方法上的 {@link DataScope} 规则集合，单个注解时集合里只有一条
     * @param rule 当前登录用户计算后的数据权限规则
     */
    public record Holder(List<DataScope> scopes, DataScopeRule rule) {
    }
}
