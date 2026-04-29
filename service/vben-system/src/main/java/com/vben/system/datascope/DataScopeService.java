package com.vben.system.datascope;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vben.common.exception.ForbiddenException;
import com.vben.system.entity.SysDept;
import com.vben.system.entity.SysRole;
import com.vben.system.entity.SysRoleDept;
import com.vben.system.entity.SysUser;
import com.vben.system.entity.SysUserDept;
import com.vben.system.entity.SysUserRole;
import com.vben.system.mapper.SysDeptMapper;
import com.vben.system.mapper.SysRoleDeptMapper;
import com.vben.system.mapper.SysRoleMapper;
import com.vben.system.mapper.SysUserDeptMapper;
import com.vben.system.mapper.SysUserRoleMapper;
import com.vben.system.auth.LoginUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据权限计算服务。
 */
@Service
@RequiredArgsConstructor
public class DataScopeService {
    private final LoginUserService loginUserService;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final SysRoleDeptMapper roleDeptMapper;
    private final SysUserDeptMapper userDeptMapper;
    private final SysDeptMapper deptMapper;

    @Value("${system.security.protected-user-id:1}")
    private Long protectedUserId;

    /**
     * 计算当前登录用户最终生效的数据权限规则。
     * <p>
     * 规则来源包括用户自身数据权限和用户拥有的启用角色数据权限，最终按并集处理：
     * 只要任一来源拥有“全部数据”，就直接返回全量访问；否则合并可访问部门集合和本人数据条件。
     *
     * @return 当前登录用户可访问的数据范围规则
     */
    public DataScopeRule resolveCurrentRule() {
        SysUser user = loginUserService.getCurrentUser();
        if (protectedUserId != null && protectedUserId.equals(user.getId())) {
            return new DataScopeRule(true, List.of(), null);
        }

        Set<Long> deptIds = new LinkedHashSet<>();
        Long selfUserId = null;

        Integer userScope = user.getDataScope();
        if (Objects.equals(userScope, DataScopeType.ALL.getValue())) {
            return new DataScopeRule(true, List.of(), null);
        }
        if (Objects.equals(userScope, DataScopeType.SELF.getValue())) {
            selfUserId = user.getId();
        }
        applyDeptScope(userScope, user.getId(), user.getDeptId(), deptIds, true);

        List<Long> roleIds = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, user.getId())
        ).stream().map(SysUserRole::getRoleId).distinct().toList();
        if (!roleIds.isEmpty()) {
            List<SysRole> roles = roleMapper.selectBatchIds(roleIds).stream()
                    .filter(Objects::nonNull)
                    .filter(role -> role.getStatus() != null && role.getStatus() == 1)
                    .toList();
            for (SysRole role : roles) {
                Integer roleScope = role.getDataScope();
                if (Objects.equals(roleScope, DataScopeType.ALL.getValue())) {
                    return new DataScopeRule(true, List.of(), null);
                }
                if (Objects.equals(roleScope, DataScopeType.SELF.getValue())) {
                    selfUserId = user.getId();
                }
                applyDeptScope(roleScope, role.getId(), user.getDeptId(), deptIds, false);
            }
        }

        return new DataScopeRule(false, new ArrayList<>(deptIds), selfUserId);
    }

    /**
     * 校验当前登录用户是否可以访问某一条具体数据。
     * <p>
     * 用于详情、编辑、删除、强制下线等非列表场景。列表查询由 MyBatis-Plus 拦截器追加 SQL 条件，
     * 但按 ID 操作时必须单独校验，避免绕过列表过滤。
     *
     * @param deptId 目标数据归属部门 ID；没有部门归属时可传 {@code null}
     * @param userId 目标数据归属用户 ID；没有本人归属时可传 {@code null}
     * @throws ForbiddenException 当前用户不在目标数据可访问范围内
     */
    public void assertAccessible(Long deptId, Long userId) {
        DataScopeRule rule = resolveCurrentRule();
        if (rule.allAccess()) {
            return;
        }
        if (rule.selfUserId() != null && Objects.equals(rule.selfUserId(), userId)) {
            return;
        }
        if (deptId != null && rule.deptIds() != null && rule.deptIds().contains(deptId)) {
            return;
        }
        throw new ForbiddenException("无权访问该数据");
    }

    /**
     * 将单个数据权限枚举转换成部门集合或本人条件，并合并到当前计算结果中。
     *
     * @param scope 数据权限类型值
     * @param ownerId 当 {@code userScope=true} 时为用户 ID，否则为角色 ID
     * @param currentDeptId 当前登录用户所属部门 ID
     * @param deptIds 正在累积的可访问部门 ID 集合
     * @param userScope 是否为用户自身数据权限；false 表示角色数据权限
     */
    private void applyDeptScope(Integer scope, Long ownerId, Long currentDeptId, Set<Long> deptIds, boolean userScope) {
        if (scope == null) {
            return;
        }
        if (Objects.equals(scope, DataScopeType.CUSTOM.getValue())) {
            deptIds.addAll(userScope ? getUserCustomDeptIds(ownerId) : getRoleCustomDeptIds(ownerId));
            return;
        }
        if (currentDeptId == null) {
            return;
        }
        if (Objects.equals(scope, DataScopeType.DEPT.getValue())) {
            deptIds.add(currentDeptId);
            return;
        }
        if (Objects.equals(scope, DataScopeType.DEPT_AND_CHILD.getValue())) {
            deptIds.addAll(getDeptAndChildIds(currentDeptId));
        }
    }

    /**
     * 查询角色“自定义数据”配置的部门集合。
     *
     * @param roleId 角色 ID
     * @return 角色自定义可访问部门 ID 集合
     */
    private List<Long> getRoleCustomDeptIds(Long roleId) {
        return roleDeptMapper.selectList(
                new LambdaQueryWrapper<SysRoleDept>().eq(SysRoleDept::getRoleId, roleId)
        ).stream().map(SysRoleDept::getDeptId).distinct().toList();
    }

    /**
     * 查询用户自身“自定义数据”配置的部门集合。
     *
     * @param userId 用户 ID
     * @return 用户自定义可访问部门 ID 集合
     */
    private List<Long> getUserCustomDeptIds(Long userId) {
        return userDeptMapper.selectList(
                new LambdaQueryWrapper<SysUserDept>().eq(SysUserDept::getUserId, userId)
        ).stream().map(SysUserDept::getDeptId).distinct().toList();
    }

    /**
     * 根据部门树计算“本部门及以下”的所有部门 ID。
     * <p>
     * 当前部门表使用 {@code id + pid} 邻接表结构，这里一次性读取部门 ID 和父 ID，
     * 在内存中从当前部门向下遍历，得到当前部门和所有子孙部门。
     *
     * @param deptId 当前登录用户所属部门 ID
     * @return 当前部门及所有下级部门 ID
     */
    private List<Long> getDeptAndChildIds(Long deptId) {
        List<SysDept> deptList = deptMapper.selectList(new LambdaQueryWrapper<SysDept>().select(SysDept::getId, SysDept::getPid));
        Map<Long, List<Long>> childIdsByPid = deptList.stream()
                .filter(dept -> dept.getPid() != null)
                .collect(Collectors.groupingBy(SysDept::getPid, Collectors.mapping(SysDept::getId, Collectors.toList())));
        Set<Long> result = new LinkedHashSet<>();
        ArrayDeque<Long> queue = new ArrayDeque<>();
        queue.add(deptId);
        while (!queue.isEmpty()) {
            Long current = queue.removeFirst();
            if (!result.add(current)) {
                continue;
            }
            List<Long> childIds = childIdsByPid.getOrDefault(current, List.of());
            queue.addAll(childIds);
        }
        return new ArrayList<>(result);
    }
}
