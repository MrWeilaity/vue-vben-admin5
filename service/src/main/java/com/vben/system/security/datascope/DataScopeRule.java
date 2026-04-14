package com.vben.system.security.datascope;

import java.util.List;

/**
 * 当前登录用户计算后的有效数据权限。
 *
 * @param allAccess 是否拥有全部数据权限；为 true 时不再追加部门或本人过滤
 * @param deptIds 可访问的部门 ID 集合，包含自定义部门、本部门、本部门及以下合并结果
 * @param selfUserId 仅本人数据对应的用户 ID；为空表示没有本人数据条件
 */
public record DataScopeRule(boolean allAccess, List<Long> deptIds, Long selfUserId) {
}
