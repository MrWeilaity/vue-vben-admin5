package com.vben.admin.controller;

import com.vben.common.ApiResponse;
import com.vben.system.dto.core.UserInfo;
import com.vben.system.dto.system.menu.MenuResponse;
import com.vben.system.service.CoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "核心接口")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CoreController {
    private final CoreService coreService;

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/user/info")
    public ApiResponse<UserInfo> info() {
        return ApiResponse.ok(coreService.getUserInfo());
    }

    @Operation(summary = "获取当前用户菜单")
    @GetMapping("/menu/all")
    public ApiResponse<List<MenuResponse>> allMenus() {
        return ApiResponse.ok(coreService.getCurrentUserMenus());
    }
}
