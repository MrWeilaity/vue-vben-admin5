package com.vben.system.controller.system;

import com.vben.system.common.ApiResponse;
import com.vben.system.common.PageResult;
import com.vben.system.dto.params.RoleParams;
import com.vben.system.dto.system.role.RoleCreateRequest;
import com.vben.system.dto.system.role.RoleResponse;
import com.vben.system.dto.system.role.RoleUpdateRequest;
import com.vben.system.service.system.impl.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器。
 */
@Tag(name = "系统管理-角色")
@RestController
@RequestMapping("/api/system/role")
@RequiredArgsConstructor
@Validated
public class SysRoleController {

    private final SysRoleService roleService;

    /**
     * 分页查询角色列表。
     *
     * @return 角色列表
     */
    @Operation(summary = "分页查询角色列表")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('System:Role:List')")
    public ApiResponse<PageResult<RoleResponse>> list(@Valid RoleParams roleParams) {

        return ApiResponse.ok(roleService.listForResponse(roleParams));
    }

    /**
     * 查询所有角色列表。
     *
     * @return 角色列表
     */
    @Operation(summary = "查询所有角色列表")
    @GetMapping("/allList")
    @PreAuthorize("hasAuthority('System:Role:List')")
    public ApiResponse<List<RoleResponse>> allList() {

        return ApiResponse.ok(roleService.allList());
    }

    /**
     * 新增角色。
     *
     * @param request 角色请求体
     * @return 空响应
     */
    @Operation(summary = "新增角色")
    @PostMapping
    @PreAuthorize("hasAuthority('System:Role:Create')")
    public ApiResponse<Void> create(@Valid @RequestBody RoleCreateRequest request) {
        roleService.create(request);
        return ApiResponse.ok(null);
    }

    /**
     * 更新角色。
     *
     * @param id      角色 ID
     * @param request 角色请求体
     * @return 空响应
     */
    @Operation(summary = "更新角色")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('System:Role:Edit')")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
        roleService.update(id, request);
        return ApiResponse.ok(null);
    }

    /**
     * 删除角色。
     *
     * @param id 角色 ID
     * @return 空响应
     */
    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('System:Role:Delete')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ApiResponse.ok(null);
    }
}
