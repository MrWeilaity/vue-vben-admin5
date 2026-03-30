package com.vben.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vben.system.common.ApiResponse;
import com.vben.system.common.exception.UnauthorizedException;
import com.vben.system.entity.SysMenu;
import com.vben.system.entity.SysRole;
import com.vben.system.entity.SysUser;
import com.vben.system.entity.SysUserRole;
import com.vben.system.mapper.SysMenuMapper;
import com.vben.system.mapper.SysRoleMapper;
import com.vben.system.mapper.SysUserMapper;
import com.vben.system.mapper.SysUserRoleMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Tag(name = "核心接口")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CoreController {
    private final ObjectMapper objectMapper;
    private final SysMenuMapper menuMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/user/info")
    public ApiResponse<Map<String, Object>> info(java.security.Principal principal) {
        if (principal == null || !StringUtils.hasText(principal.getName())) {
            throw new UnauthorizedException("未登录或登录已过期");
        }
        SysUser user = userMapper.selectOne(
            new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, principal.getName())
        );
        if (user == null) {
            throw new UnauthorizedException("用户不存在或登录已过期");
        }
        List<String> roles = loadRoles(user.getId());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("avatar", "https://unpkg.com/@vbenjs/static-source@0.1.7/source/avatar-v1.webp");
        result.put("desc", user.getRemark() == null ? "欢迎回来" : user.getRemark());
        result.put("homePath", "/dashboard/analytics");
        result.put("realName", user.getNickname());
        result.put("roles", roles);
        result.put("token", "");
        result.put("userId", String.valueOf(user.getId()));
        result.put("username", user.getUsername());
        return ApiResponse.ok(result);
    }

    @Operation(summary = "获取当前用户菜单")
    @GetMapping("/menu/all")
    public ApiResponse<List<Map<String, Object>>> allMenus() {
        List<SysMenu> menus = menuMapper.selectList(
            new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getStatus, 1)
                .orderByAsc(SysMenu::getId)
        );
        return ApiResponse.ok(buildMenuTree(menus, 0L));
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

    private List<Map<String, Object>> buildMenuTree(List<SysMenu> all, Long pid) {
        return all.stream()
            .filter(item -> item.getPid() != null && item.getPid().equals(pid))
            .sorted(Comparator.comparing(SysMenu::getId))
            .map(item -> {
                Map<String, Object> route = new LinkedHashMap<>();
                route.put("path", item.getPath());
                route.put("name", normalizeRouteName(item.getName(), item.getId()));
                route.put(
                    "component",
                    "CATALOG".equals(item.getType()) ? "BasicLayout" : item.getComponent()
                );

                Map<String, Object> meta = parseMeta(item.getMetaJson());
                if (!meta.containsKey("title")) {
                    meta.put("title", item.getName());
                }
                route.put("meta", meta);

                List<Map<String, Object>> children = buildMenuTree(all, item.getId());
                if (!children.isEmpty()) {
                    route.put("children", children);
                }
                return route;
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
}
