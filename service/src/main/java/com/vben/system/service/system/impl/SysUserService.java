package com.vben.system.service.system.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vben.system.common.PageResult;
import com.vben.system.dto.params.UserParams;
import com.vben.system.dto.system.user.UserResponse;
import com.vben.system.dto.system.user.UserUpdateRequest;
import com.vben.system.entity.SysUser;
import com.vben.system.entity.SysUserRole;
import com.vben.system.mapper.SysUserMapper;
import com.vben.system.mapper.SysUserRoleMapper;
import com.vben.system.service.AuthService;
import com.vben.system.service.system.ISysUserService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户业务服务，封装用户增删改查与下线控制。
 */
@Service
@RequiredArgsConstructor
public class SysUserService extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final AuthService authService;

    /**
     * 查询用户列表（按 ID 倒序）。
     *
     * @return 用户列表
     */
    public PageResult<UserResponse> list(UserParams userParams) {
        Page<SysUser> page = new Page<>(userParams.getPage(), userParams.getPageSize());
        Page<SysUser> result = lambdaQuery()
                .orderByDesc(SysUser::getId)
                .page(page);
        List<UserResponse> list = result.getRecords().stream()
                .map(SysUser::toUserResponse)
                .toList();
        return new PageResult<>(result.getTotal(), list);
    }

    /**
     * 新增用户。
     *
     * @param user 用户实体
     */
    @Transactional(rollbackFor = Exception.class)
    public void create(SysUser user, List<Long> roleIds) {
        userMapper.insert(user);
        saveUserRoles(user.getId(), roleIds);
    }

    /**
     * 更新用户。
     *
     * @param id         用户 ID
     * @param updateUser 用户更新请求体
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, UserUpdateRequest updateUser) {
        lambdaUpdate().eq(SysUser::getId, id)
                .set(StrUtil.isNotBlank(updateUser.getNickname()), SysUser::getNickname, updateUser.getNickname())
                .set(StrUtil.isNotBlank(updateUser.getEmail()), SysUser::getEmail, updateUser.getEmail())
                .set(StrUtil.isNotBlank(updateUser.getMobile()), SysUser::getMobile, updateUser.getMobile())
                .set(updateUser.getStatus() != null, SysUser::getStatus, updateUser.getStatus())
                .set(StrUtil.isNotBlank(updateUser.getRemark()), SysUser::getRemark, updateUser.getRemark())
                .update();
//        user.setId(id);
//        int updatedRows = userMapper.updateById(user);
//        if (updatedRows <= 0) {
//            return;
//        }
        List<Long> roleIds = updateUser.getRoleIds();
        if (CollectionUtil.isNotEmpty(roleIds)) {
            saveUserRoles(id, roleIds);
        }
    }

    /**
     * 删除用户并强制该用户 token 失效。
     *
     * @param id 用户 ID
     */
    public void delete(Long id) {
        userMapper.deleteById(id);
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
        authService.forceOffline(id);
    }

    /**
     * 强制下线用户。
     *
     * @param id 用户 ID
     */
    public void forceOffline(Long id) {
        authService.forceOffline(id);
    }

    public Map<Long, List<Long>> getRoleIdsByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        return userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, userIds))
                .stream()
                .collect(Collectors.groupingBy(SysUserRole::getUserId, Collectors.mapping(SysUserRole::getRoleId, Collectors.toList())));
    }

    /**
     * 保存用户和角色的关系
     *
     * @param userId  用户id
     * @param roleIds 角色id组合
     */
    private void saveUserRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        Set<Long> distinctRoleIds = new LinkedHashSet<>(roleIds);
        List<SysUserRole> relations = new ArrayList<>();
        for (Long roleId : distinctRoleIds) {
            if (roleId == null) {
                continue;
            }
            SysUserRole relation = new SysUserRole();
            relation.setUserId(userId);
            relation.setRoleId(roleId);
            relations.add(relation);
        }
        relations.forEach(userRoleMapper::insert);
    }
}
