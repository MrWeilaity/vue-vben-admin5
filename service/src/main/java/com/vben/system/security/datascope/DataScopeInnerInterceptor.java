package com.vben.system.security.datascope;

import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 显式 @DataScope 查询的 MyBatis-Plus SQL 数据权限拦截器。
 */
@Component
public class DataScopeInnerInterceptor implements InnerInterceptor {
    /**
     * 在 MyBatis 查询执行前，根据 {@link DataScopeAspect} 放入的线程上下文给 SQL 追加数据权限条件。
     * <p>
     * 没有 {@link DataScope}/{@link DataScopes} 注解、规则为空或拥有全部数据权限时不会改写 SQL。
     */
    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        DataScopeContext.Holder holder = DataScopeContext.get();
        if (holder == null || holder.rule() == null || holder.rule().allAccess()) {
            return;
        }
        DataScope dataScope = findMatchingDataScope(holder.scopes(), ms);
        if (dataScope == null) {
            return;
        }
        String condition = buildCondition(dataScope, holder.rule());
        if (!StringUtils.hasText(condition)) {
            return;
        }
        PluginUtils.mpBoundSql(boundSql).sql(appendCondition(boundSql.getSql(), condition));
    }

    /**
     * 从当前方法的数据权限规则中查找匹配当前 SQL 的 Mapper 规则。
     * <p>
     * 这样 @DataScope/@DataScopes 可以继续标在 service 方法上，但只影响该方法里的主业务查询，
     * 不会影响同一方法内用于回显的角色、岗位、部门等辅助查询。
     */
    private DataScope findMatchingDataScope(List<DataScope> scopes, MappedStatement ms) {
        if (scopes == null || scopes.isEmpty()) {
            return null;
        }
        for (DataScope scope : scopes) {
            if (matchesMapper(scope, ms)) {
                return scope;
            }
        }
        return null;
    }

    private boolean matchesMapper(DataScope scope, MappedStatement ms) {
        Class<?> mapper = scope.mapper();
        String statementId = ms.getId();
        if (mapper == Void.class) {
            return statementId != null;
        }
        return statementId != null && statementId.startsWith(mapper.getName() + ".");
    }

    /**
     * 将计算后的权限规则转换成 SQL 条件片段。
     * <p>
     * 部门范围和本人范围按 OR 合并；如果两者都为空，返回 {@code 1 = 0}，防止误放开数据。
     */
    private String buildCondition(DataScope dataScope, DataScopeRule rule) {
        List<String> parts = new ArrayList<>();
        if (rule.deptIds() != null && !rule.deptIds().isEmpty() && StringUtils.hasText(dataScope.deptColumn())) {
            String values = rule.deptIds().stream().map(String::valueOf).collect(Collectors.joining(","));
            parts.add(qualify(dataScope, dataScope.deptColumn()) + " IN (" + values + ")");
        }
        if (rule.selfUserId() != null && StringUtils.hasText(dataScope.userColumn())) {
            parts.add(qualify(dataScope, dataScope.userColumn()) + " = " + rule.selfUserId());
        }
        if (parts.isEmpty()) {
            return "1 = 0";
        }
        return "(" + String.join(" OR ", parts) + ")";
    }

    /**
     * 给字段补上表别名前缀。
     * <p>
     * 注解里已经传入 {@code u.dept_id} 这类完整列名时不再处理；传入普通字段时才拼接 tableAlias。
     */
    private String qualify(DataScope dataScope, String column) {
        if (column.contains(".")) {
            return column;
        }
        if (StringUtils.hasText(dataScope.tableAlias())) {
            return dataScope.tableAlias() + "." + column;
        }
        return column;
    }

    /**
     * 把数据权限条件拼到原始 SELECT SQL 中。
     * <p>
     * 目前用于 MyBatis-Plus 生成的常规查询 SQL：存在 WHERE 时追加 AND，
     * 不存在 WHERE 时新建 WHERE，并尽量保留 ORDER BY 位置。
     */
    private String appendCondition(String sql, String condition) {
        String trimmed = sql == null ? "" : sql.trim();
        String suffix = "";
        if (trimmed.endsWith(";")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
            suffix = ";";
        }
        String lower = trimmed.toLowerCase(Locale.ROOT);
        int orderIndex = lower.lastIndexOf(" order by ");
        String beforeOrder = orderIndex >= 0 ? trimmed.substring(0, orderIndex) : trimmed;
        String orderPart = orderIndex >= 0 ? trimmed.substring(orderIndex) : "";
        String beforeOrderLower = beforeOrder.toLowerCase(Locale.ROOT);
        boolean hasWhere = beforeOrderLower.contains(" where ");
        return beforeOrder + (hasWhere ? " AND " : " WHERE ") + condition + orderPart + suffix;
    }
}
