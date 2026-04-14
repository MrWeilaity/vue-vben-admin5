package com.vben.system.security.datascope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需要自动追加数据权限条件的查询方法。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataScope {
    /**
     * 需要追加数据权限的 Mapper 类型。
     * <p>
     * service 方法里可能会有多条查询，例如先查用户分页，再查角色、岗位、部门用于回显。
     * 指定 Mapper 后，拦截器只处理该 Mapper 产生的 SQL，避免同一个方法内的辅助查询被误加权限。
     */
    Class<?> mapper() default Void.class;

    /**
     * 数据所属部门列名。空值表示不按部门过滤。
     */
    String deptColumn() default "dept_id";

    /**
     * 数据所属用户列名。空值表示不按本人过滤。
     */
    String userColumn() default "create_by";

    /**
     * 表别名。传普通列名时用于拼接前缀，例如 tableAlias="u"、deptColumn="dept_id" 会生成 u.dept_id。
     */
    String tableAlias() default "";
}
