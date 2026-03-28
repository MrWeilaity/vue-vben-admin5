package com.vben.system.controller.system;

import com.vben.system.common.ApiResponse;
import com.vben.system.dto.system.user.UserCreateRequest;
import com.vben.system.dto.system.user.UserResponse;
import com.vben.system.dto.system.user.UserUpdateRequest;
import com.vben.system.entity.SysUser;
import com.vben.system.service.system.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器。
 */
@Tag(name = "系统管理-用户")
@RestController
@RequestMapping("/api/system/user")
@RequiredArgsConstructor
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
    public ApiResponse<List<UserResponse>> list(
        @RequestParam(required = false) String username,
        @RequestParam(required = false) String nickname,
        @RequestParam(required = false) Integer status
    ) {
        List<SysUser> users = userService.list(username, nickname, status);
        List<Long> userIds = users.stream().map(SysUser::getId).toList();
        var roleMap = userService.getRoleIdsByUserIds(userIds);
        List<UserResponse> data = users
            .stream()
            .map(user -> UserResponse.builder()
                .id(String.valueOf(user.getId()))
                .username(user.getUsername())
                .nickname(user.getNickname())
                .deptId(String.valueOf(user.getDeptId()))
                .email(user.getEmail())
                .mobile(user.getMobile())
                .status(user.getStatus())
                .dataScope(user.getDataScope())
                .remark(user.getRemark())
                .roleIds(roleMap.getOrDefault(user.getId(), List.of()).stream().map(String::valueOf).toList())
                .createTime(user.getCreateTime())
                .build())
            .toList();
        return ApiResponse.ok(data);
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
     * @param id   用户 ID
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
        List<Long> roleIds = request.getRoleIds() == null ? List.of() : request.getRoleIds();
        userService.update(id, user, roleIds);
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
