package com.vben.system.datascope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 多个数据权限规则容器。
 * <p>
 * 一个 service 方法内有多个业务 Mapper 查询都需要数据权限时使用；
 * 单个 Mapper 查询仍然可以直接使用 {@link DataScope}。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataScopes {
    /**
     * 当前方法内需要追加数据权限的 Mapper 规则集合。
     */
    DataScope[] value();
}
