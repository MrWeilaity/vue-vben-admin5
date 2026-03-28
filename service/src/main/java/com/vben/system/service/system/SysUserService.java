package com.vben.system.service.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vben.system.entity.SysUser;
import com.vben.system.entity.SysUserRole;
import com.vben.system.mapper.SysUserMapper;
import com.vben.system.mapper.SysUserRoleMapper;
import com.vben.system.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
public class SysUserService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final AuthService authService;

    /**
     * 查询用户列表（按 ID 倒序）。
     *
     * @return 用户列表
     */
    public List<SysUser> list(String username, String nickname, Integer status) {
        return userMapper.selectList(
            new LambdaQueryWrapper<SysUser>()
                .like(StringUtils.hasText(username), SysUser::getUsername, username)
                .like(StringUtils.hasText(nickname), SysUser::getNickname, nickname)
                .eq(status != null, SysUser::getStatus, status)
                .orderByDesc(SysUser::getId)
        );
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
     * @param id   用户 ID
     * @param user 用户实体
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, SysUser user, List<Long> roleIds) {
        user.setId(id);
        userMapper.updateById(user);
        if (roleIds != null) {
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
