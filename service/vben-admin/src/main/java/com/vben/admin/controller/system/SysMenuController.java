package com.vben.admin.controller.system;

import com.vben.common.ApiResponse;
import com.vben.system.dto.system.menu.MenuCreateRequest;
import com.vben.system.dto.system.menu.MenuResponse;
import com.vben.system.dto.system.menu.MenuUpdateRequest;
import com.vben.system.service.system.impl.SysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 菜单管理控制器。
 */
@Tag(name = "系统管理-菜单")
@RestController
@RequestMapping("/api/system/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService menuService;

    /**
     * 获取菜单列表。
     *
     * @return 菜单列表
     */
    @Operation(summary = "查询菜单列表")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('System:Menu:List')")
    public ApiResponse<List<MenuResponse>> list() {
        return ApiResponse.ok(menuService.buildMenuTree());
    }

    @Operation(summary = "校验菜单名称是否已存在")
    @GetMapping("/name-exists")
    @PreAuthorize("hasAuthority('System:Menu:List')")
    public ApiResponse<Boolean> nameExists(@RequestParam String name,
                                           @RequestParam(required = false) Long id) {
        return ApiResponse.ok(menuService.existsByName(name, id));
    }

    @Operation(summary = "校验菜单路径是否已存在")
    @GetMapping("/path-exists")
    @PreAuthorize("hasAuthority('System:Menu:List')")
    public ApiResponse<Boolean> pathExists(@RequestParam String path,
                                           @RequestParam(required = false) Long id) {
        return ApiResponse.ok(menuService.existsByPath(path, id));
    }

    /**
     * 新增菜单。
     *
     * @param request 菜单请求体
     * @return 空响应
     */
    @Operation(summary = "新增菜单")
    @PostMapping
    @PreAuthorize("hasAuthority('System:Menu:Create')")
    public ApiResponse<Void> create(@Valid @RequestBody MenuCreateRequest request) {
        menuService.create(request);
        return ApiResponse.ok(null);
    }

    /**
     * 更新菜单。
     *
     * @param id      菜单 ID
     * @param request 菜单请求体
     * @return 空响应
     */
    @Operation(summary = "更新菜单")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('System:Menu:Edit')")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody MenuUpdateRequest request) {
        menuService.update(id, request);
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
    @PreAuthorize("hasAuthority('System:Menu:Delete')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return ApiResponse.ok(null);
    }

}
