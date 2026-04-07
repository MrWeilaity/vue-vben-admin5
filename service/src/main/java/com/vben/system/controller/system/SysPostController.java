package com.vben.system.controller.system;

import com.vben.system.common.ApiResponse;
import com.vben.system.common.PageResult;
import com.vben.system.dto.params.PostParams;
import com.vben.system.dto.system.post.PostCreateRequest;
import com.vben.system.dto.system.post.PostResponse;
import com.vben.system.dto.system.post.PostUpdateRequest;
import com.vben.system.service.system.impl.SysPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 岗位管理控制器。
 */
@Tag(name = "系统管理-岗位")
@RestController
@RequestMapping("/api/system/post")
@RequiredArgsConstructor
@Validated
public class SysPostController {

    private final SysPostService postService;

    /**
     * 分页查询岗位列表。
     *
     * @param postParams 查询参数
     * @return 岗位分页数据
     */
    @Operation(summary = "分页查询岗位列表")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('System:Post:List')")
    public ApiResponse<PageResult<PostResponse>> list(@Valid PostParams postParams) {
        return ApiResponse.ok(postService.listForResponse(postParams));
    }

    /**
     * 查询全部岗位列表。
     *
     * @return 岗位列表
     */
    @Operation(summary = "查询全部岗位列表")
    @GetMapping("/allList")
    @PreAuthorize("hasAuthority('System:Post:List')")
    public ApiResponse<List<PostResponse>> allList() {
        return ApiResponse.ok(postService.allList());
    }

    /**
     * 新增岗位。
     *
     * @param request 岗位请求体
     * @return 空响应
     */
    @Operation(summary = "新增岗位")
    @PostMapping
    @PreAuthorize("hasAuthority('System:Post:Create')")
    public ApiResponse<Void> create(@Valid @RequestBody PostCreateRequest request) {
        postService.create(request);
        return ApiResponse.ok(null);
    }

    /**
     * 更新岗位。
     *
     * @param id      岗位 ID
     * @param request 岗位请求体
     * @return 空响应
     */
    @Operation(summary = "更新岗位")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('System:Post:Edit')")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody PostUpdateRequest request) {
        postService.update(id, request);
        return ApiResponse.ok(null);
    }

    /**
     * 更新岗位状态。
     *
     * @param id      岗位 ID
     * @param request 岗位请求体
     * @return 空响应
     */
    @Operation(summary = "更新岗位")
    @PutMapping("/status/{id}")
    @PreAuthorize("hasAuthority('System:Post:Edit')")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody PostUpdateRequest request) {
        postService.updateStatus(id, request.getStatus());
        return ApiResponse.ok(null);
    }

    /**
     * 删除岗位。
     *
     * @param id 岗位 ID
     * @return 空响应
     */
    @Operation(summary = "删除岗位")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('System:Post:Delete')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        postService.delete(id);
        return ApiResponse.ok(null);
    }
}
