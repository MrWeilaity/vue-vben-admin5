package com.vben.system.controller.system;

import com.vben.system.common.ApiResponse;
import com.vben.system.dto.system.menu.MenuCreateRequest;
import com.vben.system.dto.system.menu.MenuResponse;
import com.vben.system.dto.system.menu.MenuUpdateRequest;
import com.vben.system.entity.SysMenu;
import com.vben.system.service.system.impl.SysMenuService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vben.system.common.exception.ServiceException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 菜单管理控制器。
 */
@Tag(name = "系统管理-菜单")
@RestController
@RequestMapping("/api/system/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService menuService;
    private final ObjectMapper objectMapper;

    /**
     * 获取菜单列表。
     *
     * @return 菜单列表
     */
    @Operation(summary = "查询菜单列表")
    @GetMapping("/list")
    public ApiResponse<List<MenuResponse>> list() {
        List<MenuResponse> data = menuService.list()
            .stream()
            .map(menu -> MenuResponse.builder()
                .id(String.valueOf(menu.getId()))
                .pid(String.valueOf(menu.getPid()))
                .name(menu.getName())
                .path(menu.getPath())
                .type(menu.getType())
                .component(menu.getComponent())
                .authCode(menu.getAuthCode())
                .status(menu.getStatus())
                .meta(parseMeta(menu.getMetaJson()))
                .createTime(menu.getCreateTime())
                .build())
            .toList();
        return ApiResponse.ok(data);
    }

    /**
     * 新增菜单。
     *
     * @param request 菜单请求体
     * @return 空响应
     */
    @Operation(summary = "新增菜单")
    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody MenuCreateRequest request) {
        SysMenu menu = new SysMenu();
        menu.setPid(request.getPid());
        menu.setName(request.getName());
        menu.setPath(request.getPath());
        menu.setType(request.getType());
        menu.setComponent(request.getComponent());
        menu.setAuthCode(request.getAuthCode());
        menu.setStatus(request.getStatus());
        menu.setMetaJson(writeMeta(request.getMeta()));
        menuService.create(menu);
        return ApiResponse.ok(null);
    }

    /**
     * 更新菜单。
     *
     * @param id   菜单 ID
     * @param request 菜单请求体
     * @return 空响应
     */
    @Operation(summary = "更新菜单")
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody MenuUpdateRequest request) {
        SysMenu menu = new SysMenu();
        menu.setPid(request.getPid());
        menu.setName(request.getName());
        menu.setPath(request.getPath());
        menu.setType(request.getType());
        menu.setComponent(request.getComponent());
        menu.setAuthCode(request.getAuthCode());
        menu.setStatus(request.getStatus());
        menu.setMetaJson(writeMeta(request.getMeta()));
        menuService.update(id, menu);
        return ApiResponse.ok(null);
    }

    /**
     * 删除菜单。
     *
     * @param id 菜单 ID
     * @return 空响应
     */
    @Operation(summary = "删除菜单")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return ApiResponse.ok(null);
    }

    private Map<String, Object> parseMeta(String metaJson) {
        if (metaJson == null || metaJson.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(metaJson, new TypeReference<>() {
            });
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }

    private String writeMeta(Map<String, Object> meta) {
        if (meta == null || meta.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(meta);
        } catch (Exception ex) {
            throw new ServiceException("菜单 meta 字段不是合法 JSON");
        }
    }
}
