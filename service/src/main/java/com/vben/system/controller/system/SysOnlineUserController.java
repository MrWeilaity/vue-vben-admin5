package com.vben.system.controller.system;

import com.vben.system.common.ApiResponse;
import com.vben.system.dto.system.user.OnlineUserResponse;
import com.vben.system.service.system.ISysOnlineUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "系统管理-在线用户")
@RestController
@RequestMapping("/api/system/online")
@RequiredArgsConstructor
public class SysOnlineUserController {

    private final ISysOnlineUserService onlineUserService;

    @Operation(summary = "查询在线用户")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('System:Online:List')")
    public ApiResponse<List<OnlineUserResponse>> list() {
        return ApiResponse.ok(onlineUserService.list());
    }

    @Operation(summary = "下线指定会话")
    @PostMapping("/{sessionId}/offline")
    @PreAuthorize("hasAuthority('System:Online:Offline')")
    public ApiResponse<Void> offline(@PathVariable String sessionId) {
        onlineUserService.offline(sessionId);
        return ApiResponse.ok(null);
    }
}
