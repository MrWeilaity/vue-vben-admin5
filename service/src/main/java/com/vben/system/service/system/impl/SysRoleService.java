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
import com.vben.system.entity.SysRoleMenu;
import com.vben.system.entity.SysUserRole;
import com.vben.system.mapper.SysRoleMapper;
import com.vben.system.mapper.SysRoleMenuMapper;
import com.vben.system.mapper.SysUserRoleMapper;
import com.vben.system.service.system.ISysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 角色业务服务，处理角色增删改查。
 */
@Service
@RequiredArgsConstructor
public class SysRoleService extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMenuMapper roleMenuMapper;

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
        List<RoleResponse> list = result.getRecords().stream()
                .map(role -> RoleResponse.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .status(role.getStatus())
                        .permissions(role.getPermissions())
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
    public void create(RoleCreateRequest createRequest) {
        SysRole sysRole = new SysRole();
        sysRole.setName(createRequest.getName());
        sysRole.setStatus(createRequest.getStatus());
        sysRole.setPermissions(createRequest.getPermissions());
        sysRole.setRemark(createRequest.getRemark());
        roleMapper.insert(sysRole);
    }

    /**
     * 更新角色。
     *
     * @param id            角色 ID
     * @param updateRequest 编辑角色实体
     */
    public void update(Long id, RoleUpdateRequest updateRequest) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new IllegalArgumentException("角色不存在");
        }
        role.setName(updateRequest.getName());
        role.setStatus(updateRequest.getStatus());
        role.setPermissions(updateRequest.getPermissions());
        role.setRemark(updateRequest.getRemark());
        roleMapper.updateById(role);
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
            throw new ServiceException("角色不存在");
        }
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, id));
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
        return sysRoles.stream()
                        .map(role -> RoleResponse.builder()
                                .id(role.getId())
                                .name(role.getName())
                                .status(role.getStatus())
                                .permissions(role.getPermissions())
                                .remark(role.getRemark())
                                .createTime(role.getCreateTime())
                                .build())
                        .toList();
    }
}
