package com.vben.system.controller.system;

import com.vben.system.common.ApiResponse;
import com.vben.system.common.PageResult;
import com.vben.system.dto.params.UserParams;
import com.vben.system.dto.system.user.UserCreateRequest;
import com.vben.system.dto.system.user.UserResponse;
import com.vben.system.dto.system.user.UserUpdateRequest;
import com.vben.system.entity.SysUser;
import com.vben.system.service.system.impl.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器。
 */
@Tag(name = "系统管理-用户")
@RestController
@RequestMapping("/api/system/user")
@RequiredArgsConstructor
@Validated
public class SysUserController {

    private final SysUserService userService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 获取用户列表。
     *
     * @return 用户列表
     */
    @Operation(summary = "查询用户列表")
    @GetMapping("/list")
    public ApiResponse<PageResult<UserResponse>> list(@Valid UserParams userParams) {
        return ApiResponse.ok(userService.list(userParams));
    }

    /**
     * 新增用户。
     *
     * @param request 用户请求体
     * @return 空响应
     */
    @Operation(summary = "新增用户")
    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody UserCreateRequest request) {
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setNickname(request.getNickname());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDeptId(request.getDeptId());
        user.setEmail(request.getEmail());
        user.setMobile(request.getMobile());
        user.setStatus(request.getStatus());
        user.setDataScope(request.getDataScope());
        user.setRemark(request.getRemark());
        List<Long> roleIds = request.getRoleIds() == null ? List.of() : request.getRoleIds();
        userService.create(user, roleIds);
        return ApiResponse.ok(null);
    }

    /**
     * 更新用户。
     *
     * @param id      用户 ID
     * @param request 用户请求体
     * @return 空响应
     */
    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        SysUser user = new SysUser();
        user.setNickname(request.getNickname());
        user.setDeptId(request.getDeptId());
        user.setEmail(request.getEmail());
        user.setMobile(request.getMobile());
        user.setStatus(request.getStatus());
        user.setDataScope(request.getDataScope());
        user.setRemark(request.getRemark());
        userService.update(id, user, request.getRoleIds());
        return ApiResponse.ok(null);
    }

    /**
     * 删除用户。
     *
     * @param id 用户 ID
     * @return 空响应
     */
    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ApiResponse.ok(null);
    }

    /**
     * 强制下线用户。
     *
     * @param id 用户 ID
     * @return 空响应
     */
    @Operation(summary = "强制下线用户")
    @PostMapping("/{id}/force-offline")
    public ApiResponse<Void> forceOffline(@PathVariable Long id) {
        userService.forceOffline(id);
        return ApiResponse.ok(null);
    }
}
