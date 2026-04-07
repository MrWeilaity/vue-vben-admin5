package com.vben.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vben.system.dto.core.UserInfo;
import com.vben.system.dto.system.menu.MenuResponse;
import com.vben.system.entity.SysMenu;
import com.vben.system.entity.SysRole;
import com.vben.system.entity.SysUser;
import com.vben.system.entity.SysRoleMenu;
import com.vben.system.entity.SysUserRole;
import com.vben.system.mapper.SysMenuMapper;
import com.vben.system.mapper.SysRoleMapper;
import com.vben.system.mapper.SysRoleMenuMapper;
import com.vben.system.mapper.SysUserRoleMapper;
import com.vben.system.security.LoginUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * CoreService 组件说明。
 */
@Service
@RequiredArgsConstructor
public class CoreService {
    private final LoginUserService loginUserService;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysMenuMapper menuMapper;
    private final ObjectMapper objectMapper;

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

    public List<MenuResponse> getCurrentUserMenus() {
        SysUser user = loginUserService.getCurrentUser();
        if (user.getStatus() == null || user.getStatus() != 1) {
            return List.of();
        }

        List<Long> enabledRoleIds = getEnabledRoleIds(user.getId());
        if (enabledRoleIds.isEmpty()) {
            return List.of();
        }

        List<Long> menuIds = roleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, enabledRoleIds)
        ).stream().map(SysRoleMenu::getMenuId).distinct().toList();
        if (menuIds.isEmpty()) {
            return List.of();
        }

        List<SysMenu> menus = menuMapper.selectBatchIds(menuIds).stream()
                .filter(Objects::nonNull)
                .filter(menu -> menu.getStatus() != null && menu.getStatus() == 1)
                .toList();
        return buildMenuTree(menus, null);
    }

    private List<String> loadRoles(Long userId) {
        List<Long> enabledRoleIds = getEnabledRoleIds(userId);
        if (enabledRoleIds.isEmpty()) {
            return List.of();
        }
        return roleMapper.selectBatchIds(enabledRoleIds).stream()
                .filter(Objects::nonNull)
                .map(SysRole::getName)
                .filter(StringUtils::hasText)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .toList();
    }

    /**
     * 查询当前用户的未被禁用的角色id
     *
     * @param userId 用户id
     * @return 角色id列表
     */
    private List<Long> getEnabledRoleIds(Long userId) {
        List<Long> roleIds = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
        ).stream().map(SysUserRole::getRoleId).toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return roleMapper.selectBatchIds(roleIds).stream()
                .filter(Objects::nonNull)
                .filter(role -> role.getStatus() != null && role.getStatus() == 1)
                .map(SysRole::getId)
                .distinct()
                .toList();
    }

    private List<MenuResponse> buildMenuTree(List<SysMenu> all, Long pid) {
        return all.stream()
                .filter(item -> Objects.equals(item.getPid(), pid))
                .filter(item -> !"button".equals(normalizeMenuType(item.getType())))
                .sorted(Comparator.comparing(SysMenu::getId))
                .map(item -> {
                    String menuType = normalizeMenuType(item.getType());
                    Map<String, Object> meta = parseMeta(item.getMetaJson());
                    if (!meta.containsKey("title")) {
                        meta.put("title", item.getName());
                    }

                    List<MenuResponse> children = buildMenuTree(all, item.getId());
                    return MenuResponse.builder()
                            .id(item.getId())
                            .pid(item.getPid())
                            .name(normalizeRouteName(item.getName(), item.getId()))
                            .path(item.getPath())
                            .type(menuType)
                            .component(item.getComponent())
                            .authCode(item.getAuthCode())
                            .status(item.getStatus())
                            .meta(meta)
                            .children(children.isEmpty() ? null : children)
                            .build();
                })
                .toList();
    }

    private String normalizeRouteName(String name, Long id) {
        if (!StringUtils.hasText(name)) {
            return "menu_" + id;
        }
        return name.replaceAll("\\s+", "");
    }

    private Map<String, Object> parseMeta(String metaJson) {
        if (!StringUtils.hasText(metaJson)) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(metaJson, new TypeReference<>() {
            });
        } catch (Exception ex) {
            return new LinkedHashMap<>();
        }
    }

    private String normalizeMenuType(String type) {
        return type == null ? null : type.toLowerCase(Locale.ROOT);
    }
}
