package com.vben.admin.controller.system;

import com.vben.common.ApiResponse;
import com.vben.system.dto.system.dept.DeptCreateRequest;
import com.vben.system.dto.system.dept.DeptResponse;
import com.vben.system.dto.system.dept.DeptUpdateRequest;
import com.vben.system.service.system.impl.SysDeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理控制器。
 */
@Tag(name = "系统管理-部门")
@RestController
@RequestMapping("/api/system/dept")
@RequiredArgsConstructor
public class SysDeptController {

    private final SysDeptService deptService;

    /**
     * 获取部门列表。
     *
     * @return 部门列表
     */
    @Operation(summary = "查询部门列表")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('System:Dept:List')")
    public ApiResponse<List<DeptResponse>> list() {
        return ApiResponse.ok(deptService.treeDeptList());
    }

    /**
     * 新增部门。
     *
     * @param request 部门请求体
     * @return 空响应
     */
    @Operation(summary = "新增部门")
    @PostMapping
    @PreAuthorize("hasAuthority('System:Dept:Create')")
    public ApiResponse<Void> create(@Valid @RequestBody DeptCreateRequest request) {
        deptService.create(request);
        return ApiResponse.ok(null);
    }

    /**
     * 更新部门。
     *
     * @param id      部门 ID
     * @param request 部门请求体
     * @return 空响应
     */
    @Operation(summary = "更新部门")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('System:Dept:Edit')")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody DeptUpdateRequest request) {
        deptService.update(id, request);
        return ApiResponse.ok(null);
    }

    /**
     * 删除部门。
     *
     * @param id 部门 ID
     * @return 空响应
     */
    @Operation(summary = "删除部门")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('System:Dept:Delete')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        deptService.delete(id);
        return ApiResponse.ok(null);
    }
}
