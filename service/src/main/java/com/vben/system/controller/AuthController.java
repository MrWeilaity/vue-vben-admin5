package com.vben.system.controller;

import com.vben.system.common.ApiResponse;
import com.vben.system.dto.auth.CaptchaResponse;
import com.vben.system.dto.auth.LoginRequest;
import com.vben.system.dto.auth.RefreshTokenRequest;
import com.vben.system.dto.auth.TokenResponse;
import com.vben.system.service.AuthService;
import com.vben.system.util.RequestIpResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/**
 * 认证控制器。
 */
@Tag(name = "认证中心")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RequestIpResolver requestIpResolver;

    @Operation(summary = "账号密码登录", description = "完成验证码校验、登录失败次数校验并签发 Access/Refresh Token")
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {
        return ApiResponse.ok(authService.login(request, requestIpResolver.resolve(httpServletRequest)));
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
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) Map<String, String> payload
    ) {
        String refreshToken = payload == null ? null : payload.get("refreshToken");
        String accessToken = null;

        if (authorization != null && authorization.startsWith("Bearer ")) {
            accessToken = authorization.substring(7);
        }
        authService.logout(accessToken, refreshToken);
        return ApiResponse.ok(null);
    }

    @Operation(summary = "获取当前用户权限码", description = "根据当前登录用户返回权限码数组")
    @GetMapping("/codes")
    public ApiResponse<java.util.List<String>> codes() {
        return ApiResponse.ok(authService.getAccessCodes());
    }

    @Operation(summary = "获取登录验证码", description = "生成4位数字字母组合验证码图片（Base64）并写入 Redis（校验不区分大小写）")
    @GetMapping("/captcha")
    public ApiResponse<CaptchaResponse> captcha() {
        String captchaKey = UUID.randomUUID().toString().replace("-", "");
        int expireSeconds = 120;
        AuthService.CaptchaPayload captchaPayload = authService.generateCaptcha(captchaKey, Duration.ofSeconds(expireSeconds));
        return ApiResponse.ok(CaptchaResponse.builder()
                .captchaKey(captchaKey)
                .captchaImageBase64(captchaPayload.captchaImageBase64())
                .expireSeconds(expireSeconds)
                .build());
    }
}
