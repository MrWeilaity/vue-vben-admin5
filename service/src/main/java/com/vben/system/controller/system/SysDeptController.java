package com.vben.system.controller.system;

import com.vben.system.common.ApiResponse;
import com.vben.system.dto.system.dept.DeptCreateRequest;
import com.vben.system.dto.system.dept.DeptResponse;
import com.vben.system.dto.system.dept.DeptUpdateRequest;
import com.vben.system.entity.SysDept;
import com.vben.system.service.system.SysDeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public ApiResponse<List<DeptResponse>> list() {
        List<DeptResponse> data = deptService.list()
            .stream()
            .map(dept -> DeptResponse.builder()
                .id(String.valueOf(dept.getId()))
                .pid(String.valueOf(dept.getPid()))
                .name(dept.getName())
                .status(dept.getStatus())
                .remark(dept.getRemark())
                .createTime(dept.getCreateTime())
                .build())
            .toList();
        return ApiResponse.ok(data);
    }

    /**
     * 新增部门。
     *
     * @param request 部门请求体
     * @return 空响应
     */
    @Operation(summary = "新增部门")
    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody DeptCreateRequest request) {
        SysDept dept = new SysDept();
        dept.setPid(request.getPid());
        dept.setName(request.getName());
        dept.setStatus(request.getStatus());
        dept.setRemark(request.getRemark());
        deptService.create(dept);
        return ApiResponse.ok(null);
    }

    /**
     * 更新部门。
     *
     * @param id   部门 ID
     * @param request 部门请求体
     * @return 空响应
     */
    @Operation(summary = "更新部门")
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody DeptUpdateRequest request) {
        SysDept dept = new SysDept();
        dept.setPid(request.getPid());
        dept.setName(request.getName());
        dept.setStatus(request.getStatus());
        dept.setRemark(request.getRemark());
        deptService.update(id, dept);
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
    public ApiResponse<Void> delete(@PathVariable Long id) {
        deptService.delete(id);
        return ApiResponse.ok(null);
    }
}
