package com.vben.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vben.system.dto.core.UserInfo;
import com.vben.system.entity.SysRole;
import com.vben.system.entity.SysUser;
import com.vben.system.entity.SysUserRole;
import com.vben.system.mapper.SysRoleMapper;
import com.vben.system.mapper.SysUserRoleMapper;
import com.vben.system.security.LoginUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * CoreService 组件说明。
 */
@Service
@RequiredArgsConstructor
public class CoreService {
    private final LoginUserService loginUserService;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;

    public UserInfo getUserInfo() {
        SysUser user = loginUserService.getCurrentUser();
        List<String> roles = loadRoles(user.getId());
        UserInfo userInfo = new UserInfo();
        userInfo.setDesc(user.getRemark() == null ? "欢迎回来" : user.getRemark());
        userInfo.setUserId(String.valueOf(user.getId()));
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getNickname());
        userInfo.setAvatar("https://unpkg.com/@vbenjs/static-source@0.1.7/source/avatar-v1.webp");
        userInfo.setHomePath("/analytics");
        userInfo.setRoles(roles);
        return userInfo;


    }

    private List<String> loadRoles(Long userId) {
        List<Long> roleIds = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
        ).stream().map(SysUserRole::getRoleId).toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return roleMapper.selectBatchIds(roleIds).stream()
                .map(SysRole::getName)
                .filter(StringUtils::hasText)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .toList();
    }
}
