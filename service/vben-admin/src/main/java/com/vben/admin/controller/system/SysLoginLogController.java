package com.vben.admin.controller.system;

import com.vben.common.ApiResponse;
import com.vben.common.PageResult;
import com.vben.system.dto.params.LoginLogParams;
import com.vben.system.entity.SysLoginLog;
import com.vben.system.service.system.ISysLoginLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "系统管理-登录日志")
@RestController
@RequestMapping("/api/system/log/login")
@RequiredArgsConstructor
@Validated
public class SysLoginLogController {

    private final ISysLoginLogService loginLogService;

    @Operation(summary = "查询登录日志列表")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('System:LoginLog:List')")
    public ApiResponse<PageResult<SysLoginLog>> list(@Valid LoginLogParams params) {
        return ApiResponse.ok(loginLogService.getList(params));
    }
}
