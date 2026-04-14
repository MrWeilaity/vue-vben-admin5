package com.vben.system.service.system.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vben.system.common.PageResult;
import com.vben.system.common.exception.ServiceException;
import com.vben.system.dto.params.RoleParams;
import com.vben.system.dto.system.role.RoleCreateRequest;
import com.vben.system.dto.system.role.RoleResponse;
import com.vben.system.dto.system.role.RoleUpdateRequest;
import com.vben.system.entity.SysRole;
import com.vben.system.entity.SysRoleDept;
import com.vben.system.entity.SysRoleMenu;
import com.vben.system.entity.SysUserRole;
import com.vben.system.mapper.SysRoleDeptMapper;
import com.vben.system.mapper.SysRoleMapper;
import com.vben.system.mapper.SysRoleMenuMapper;
import com.vben.system.mapper.SysUserRoleMapper;
import com.vben.system.security.datascope.DataScopeType;
import com.vben.system.service.system.ISysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色业务服务，处理角色增删改查。
 */
@Service
@RequiredArgsConstructor
public class SysRoleService extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysRoleDeptMapper roleDeptMapper;

    /**
     * 查询角色列表（按 ID 倒序）。
     *
     * @return 角色列表
     */
    public List<SysRole> list() {
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>().orderByDesc(SysRole::getId));
    }

    /**
     * 查询角色列表返回给前端
     */
    public PageResult<RoleResponse> listForResponse(RoleParams roleParams) {
        LocalDate startTime = roleParams.getStartTime();
        LocalDate endTime = roleParams.getEndTime();
        if (startTime != null
                && endTime != null
                && startTime.isAfter(endTime)) {
            throw new ServiceException("开始时间不能大于结束时间");
        }

        Page<SysRole> page = new Page<>(roleParams.getPage(), roleParams.getPageSize());
        LambdaQueryChainWrapper<SysRole> lambdaQueryChainWrapper = lambdaQuery()
                .eq(roleParams.getStatus() != null, SysRole::getStatus, roleParams.getStatus())
                .like(StrUtil.isNotBlank(roleParams.getName()), SysRole::getName, roleParams.getName())
                .like(StrUtil.isNotBlank(roleParams.getRemark()), SysRole::getRemark, roleParams.getRemark());
        if (startTime != null) {
            lambdaQueryChainWrapper.ge(
                    SysRole::getCreateTime,
                    startTime.atStartOfDay());
        }
        if (endTime != null) {
            lambdaQueryChainWrapper.lt(
                    SysRole::getCreateTime,
                    endTime.plusDays(1).atStartOfDay());
        }


        Page<SysRole> result = lambdaQueryChainWrapper.orderByDesc(SysRole::getId)
                .page(page);
        Map<Long, List<Long>> menuIdsByRoleId = getMenuIdsByRoleIds(
                result.getRecords().stream().map(SysRole::getId).toList()
        );
        Map<Long, List<Long>> dataScopeDeptIdsByRoleId = getDataScopeDeptIdsByRoleIds(
                result.getRecords().stream().map(SysRole::getId).toList()
        );
        List<RoleResponse> list = result.getRecords().stream()
                .map(role -> RoleResponse.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .status(role.getStatus())
                        .dataScope(role.getDataScope())
                        .permissions(menuIdsByRoleId.getOrDefault(role.getId(), List.of()))
                        .dataScopeDeptIds(dataScopeDeptIdsByRoleId.getOrDefault(role.getId(), List.of()))
                        .remark(role.getRemark())
                        .createTime(role.getCreateTime())
                        .build())
                .toList();
        return new PageResult<>(result.getTotal(), list);
    }

    /**
     * 新增角色。
     *
     * @param createRequest 新增角色实体
     */
    @Transactional(rollbackFor = Exception.class)
    public void create(RoleCreateRequest createRequest) {
        validateDataScope(createRequest.getDataScope(), createRequest.getDataScopeDeptIds());
        SysRole sysRole = new SysRole();
        sysRole.setName(createRequest.getName());
        sysRole.setStatus(createRequest.getStatus());
        sysRole.setDataScope(createRequest.getDataScope());
        sysRole.setPermissions(createRequest.getPermissions());
        sysRole.setRemark(createRequest.getRemark());
        roleMapper.insert(sysRole);
        syncRoleMenus(sysRole.getId(), createRequest.getPermissions());
        syncRoleDepts(sysRole.getId(), createRequest.getDataScope(), createRequest.getDataScopeDeptIds());
    }

    /**
     * 更新角色。
     *
     * @param id            角色 ID
     * @param updateRequest 编辑角色实体
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, RoleUpdateRequest updateRequest) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new ServiceException("角色不存在");
        }
        Integer dataScope = updateRequest.getDataScope() == null ? role.getDataScope() : updateRequest.getDataScope();
        if (updateRequest.getDataScope() != null || updateRequest.getDataScopeDeptIds() != null) {
            validateDataScope(dataScope, updateRequest.getDataScopeDeptIds());
        }
        role.setName(updateRequest.getName());
        role.setStatus(updateRequest.getStatus());
        role.setDataScope(updateRequest.getDataScope());
        role.setPermissions(updateRequest.getPermissions());
        role.setRemark(updateRequest.getRemark());
        roleMapper.updateById(role);
        syncRoleMenus(id, updateRequest.getPermissions());
        if (updateRequest.getDataScope() != null || updateRequest.getDataScopeDeptIds() != null) {
            syncRoleDepts(id, dataScope, updateRequest.getDataScopeDeptIds());
        }
    }

    /**
     * 删除角色。
     *
     * @param id 角色 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        SysRole sysRole = roleMapper.selectById(id);
        if (sysRole == null) {
            throw new ServiceException("角色不存在或已被删除");
        }
        Long userCount = userRoleMapper.selectCount(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, id)
        );
        if (userCount != null && userCount > 0) {
            throw new ServiceException("该角色已分配给 " + userCount + " 个用户，请先解除用户关联后再删除");
        }
        roleDeptMapper.delete(new LambdaQueryWrapper<SysRoleDept>().eq(SysRoleDept::getRoleId, id));
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        roleMapper.deleteById(id);
    }

    /**
     * 返回所有的角色
     */
    public List<RoleResponse> allList() {
        List<SysRole> sysRoles = baseMapper.selectList(null);
        if (CollectionUtil.isEmpty(sysRoles)) {
            return null;
        }
        Map<Long, List<Long>> menuIdsByRoleId = getMenuIdsByRoleIds(
                sysRoles.stream().map(SysRole::getId).toList()
        );
        Map<Long, List<Long>> dataScopeDeptIdsByRoleId = getDataScopeDeptIdsByRoleIds(
                sysRoles.stream().map(SysRole::getId).toList()
        );
        return sysRoles.stream()
                .map(role -> RoleResponse.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .status(role.getStatus())
                        .dataScope(role.getDataScope())
                        .permissions(menuIdsByRoleId.getOrDefault(role.getId(), List.of()))
                        .dataScopeDeptIds(dataScopeDeptIdsByRoleId.getOrDefault(role.getId(), List.of()))
                        .remark(role.getRemark())
                        .createTime(role.getCreateTime())
                        .build())
                .toList();
    }

    private Map<Long, List<Long>> getMenuIdsByRoleIds(List<Long> roleIds) {
        if (CollectionUtil.isEmpty(roleIds)) {
            return Map.of();
        }
        return roleMenuMapper.selectList(
                        new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds)
                ).stream()
                .collect(Collectors.groupingBy(
                        SysRoleMenu::getRoleId,
                        Collectors.mapping(SysRoleMenu::getMenuId, Collectors.toList())
                ));
    }

    /**
     * 批量查询角色自定义数据权限部门，用于角色列表和角色下拉回显。
     *
     * @param roleIds 角色 ID 集合
     * @return key 为角色 ID，value 为该角色配置的自定义部门 ID 集合
     */
    private Map<Long, List<Long>> getDataScopeDeptIdsByRoleIds(List<Long> roleIds) {
        if (CollectionUtil.isEmpty(roleIds)) {
            return Map.of();
        }
        return roleDeptMapper.selectList(
                        new LambdaQueryWrapper<SysRoleDept>().in(SysRoleDept::getRoleId, roleIds)
                ).stream()
                .collect(Collectors.groupingBy(
                        SysRoleDept::getRoleId,
                        Collectors.mapping(SysRoleDept::getDeptId, Collectors.toList())
                ));
    }

    private void syncRoleMenus(Long roleId, List<Long> menuIds) {
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        if (CollectionUtil.isEmpty(menuIds)) {
            return;
        }

        Set<Long> distinctMenuIds = new LinkedHashSet<>(menuIds);
        List<SysRoleMenu> relations = new ArrayList<>();
        for (Long menuId : distinctMenuIds) {
            if (menuId == null) {
                continue;
            }
            SysRoleMenu relation = new SysRoleMenu();
            relation.setRoleId(roleId);
            relation.setMenuId(menuId);
            relations.add(relation);
        }
        relations.forEach(roleMenuMapper::insert);
    }

    /**
     * 同步角色和自定义数据权限部门的关系。
     * <p>
     * 每次保存都先删除旧关系；只有 {@code dataScope=2}（自定义数据）时才写入新部门集合，
     * 其它数据权限类型会清空原有自定义部门，避免历史配置残留。
     *
     * @param roleId    角色 ID
     * @param dataScope 数据权限类型
     * @param deptIds   自定义部门 ID 集合
     */
    private void syncRoleDepts(Long roleId, Integer dataScope, List<Long> deptIds) {
        roleDeptMapper.delete(new LambdaQueryWrapper<SysRoleDept>().eq(SysRoleDept::getRoleId, roleId));
        if (!java.util.Objects.equals(DataScopeType.CUSTOM.getValue(), dataScope)) {
            return;
        }
        Set<Long> distinctDeptIds = new LinkedHashSet<>(deptIds);
        List<SysRoleDept> relations = new ArrayList<>();
        for (Long deptId : distinctDeptIds) {
            if (deptId == null) {
                continue;
            }
            SysRoleDept relation = new SysRoleDept();
            relation.setRoleId(roleId);
            relation.setDeptId(deptId);
            relations.add(relation);
        }
        relations.forEach(roleDeptMapper::insert);
    }

    /**
     * 校验角色数据权限配置是否有效。
     * <p>
     * 数据权限必须是 1-5；选择自定义数据时必须至少选择一个部门。
     */
    private void validateDataScope(Integer dataScope, List<Long> deptIds) {
        if (!DataScopeType.isValid(dataScope)) {
            throw new ServiceException("数据权限范围不合法");
        }
        if (java.util.Objects.equals(DataScopeType.CUSTOM.getValue(), dataScope) && CollectionUtil.isEmpty(deptIds)) {
            throw new ServiceException("自定义数据权限必须选择部门");
        }
    }
}
