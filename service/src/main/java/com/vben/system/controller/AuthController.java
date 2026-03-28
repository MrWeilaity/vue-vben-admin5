package com.vben.system.controller;

import com.vben.system.common.ApiResponse;
import com.vben.system.dto.auth.LoginRequest;
import com.vben.system.dto.auth.RefreshTokenRequest;
import com.vben.system.dto.auth.TokenResponse;
import com.vben.system.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器。
 */
@Tag(name = "认证中心")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "账号密码登录", description = "完成验证码校验、登录失败次数校验并签发 Access/Refresh Token")
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {
        return ApiResponse.ok(authService.login(request, httpServletRequest.getRemoteAddr()));
    }

    @Operation(summary = "刷新访问令牌", description = "使用 Refresh Token 换取新的 Access Token 与 Refresh Token")
    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(
        @RequestBody(required = false) RefreshTokenRequest request,
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        String refreshToken = request == null ? null : request.getRefreshToken();
        if (!StringUtils.hasText(refreshToken) && StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            refreshToken = authorization.substring(7);
        }
        return ApiResponse.ok(authService.refresh(refreshToken));
    }

    @Operation(summary = "退出登录", description = "将当前 Access Token 加入黑名单并移除 Refresh Token")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
        @RequestHeader("Authorization") String authorization,
        @RequestBody(required = false) Map<String, String> payload
    ) {
        String refreshToken = payload == null ? null : payload.get("refreshToken");
        authService.logout(authorization.replace("Bearer ", ""), refreshToken);
        return ApiResponse.ok(null);
    }
}
