package com.vben.system.controller.system;

import com.vben.system.common.ApiResponse;
import com.vben.system.dto.system.role.RoleCreateRequest;
import com.vben.system.dto.system.role.RoleResponse;
import com.vben.system.dto.system.role.RoleUpdateRequest;
import com.vben.system.entity.SysRole;
import com.vben.system.service.system.impl.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器。
 */
@Tag(name = "系统管理-角色")
@RestController
@RequestMapping("/api/system/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService roleService;

    /**
     * 获取角色列表。
     *
     * @return 角色列表
     */
    @Operation(summary = "查询角色列表")
    @GetMapping("/list")
    public ApiResponse<List<RoleResponse>> list() {
        List<RoleResponse> data = roleService.list()
            .stream()
            .map(role -> RoleResponse.builder()
                .id(String.valueOf(role.getId()))
                .name(role.getName())
                .status(role.getStatus())
                .remark(role.getRemark())
                .createTime(role.getCreateTime())
                .permissions(List.of())
                .build())
            .toList();
        return ApiResponse.ok(data);
    }

    /**
     * 新增角色。
     *
     * @param request 角色请求体
     * @return 空响应
     */
    @Operation(summary = "新增角色")
    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody RoleCreateRequest request) {
        SysRole role = new SysRole();
        role.setName(request.getName());
        role.setStatus(request.getStatus());
        role.setRemark(request.getRemark());
        roleService.create(role);
        return ApiResponse.ok(null);
    }

    /**
     * 更新角色。
     *
     * @param id   角色 ID
     * @param request 角色请求体
     * @return 空响应
     */
    @Operation(summary = "更新角色")
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
        SysRole role = new SysRole();
        role.setName(request.getName());
        role.setStatus(request.getStatus());
        role.setRemark(request.getRemark());
        roleService.update(id, role);
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
    public ApiResponse<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ApiResponse.ok(null);
    }
}
