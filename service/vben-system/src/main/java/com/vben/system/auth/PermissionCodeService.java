package com.vben.system.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vben.system.entity.SysMenu;
import com.vben.system.entity.SysRole;
import com.vben.system.entity.SysRoleMenu;
import com.vben.system.entity.SysUser;
import com.vben.system.entity.SysUserRole;
import com.vben.system.mapper.SysMenuMapper;
import com.vben.system.mapper.SysRoleMapper;
import com.vben.system.mapper.SysRoleMenuMapper;
import com.vben.system.mapper.SysUserMapper;
import com.vben.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PermissionCodeService {
    @Value("${system.security.protected-user-id:1}")
    private Long protectedUserId;

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysMenuMapper menuMapper;
    private final SysRoleMapper roleMapper;

    public List<String> getAccessCodesByUserId(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            return List.of();
        }

        if (Objects.equals(userId, protectedUserId)) {
            return menuMapper.selectList(
                            new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getStatus, 1)
                    ).stream()
                    .map(SysMenu::getAuthCode)
                    .filter(StringUtils::hasText)
                    .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new))
                    .stream()
                    .sorted()
                    .toList();
        }

        List<Long> roleIds = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
        ).stream().map(SysUserRole::getRoleId).distinct().toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }

        List<Long> enabledRoleIds = roleMapper.selectBatchIds(roleIds).stream()
                .filter(Objects::nonNull)
                .filter(role -> role.getStatus() != null && role.getStatus() == 1)
                .map(SysRole::getId)
                .distinct()
                .toList();
        if (enabledRoleIds.isEmpty()) {
            return List.of();
        }

        List<Long> menuIds = roleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, enabledRoleIds)
        ).stream().map(SysRoleMenu::getMenuId).distinct().toList();
        if (menuIds.isEmpty()) {
            return List.of();
        }
        return menuMapper.selectBatchIds(menuIds).stream()
                .filter(Objects::nonNull)
                .filter(menu -> menu.getStatus() != null && menu.getStatus() == 1)
                .map(SysMenu::getAuthCode)
                .filter(StringUtils::hasText)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .sorted()
                .toList();
    }
}
