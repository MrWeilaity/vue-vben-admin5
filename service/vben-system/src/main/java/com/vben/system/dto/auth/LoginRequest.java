package com.vben.system.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求参数。
 */
@Data
public class LoginRequest {
    /**
     * 用户名
     */
    @NotBlank(message = "不能为空")
    private String username;
    /**
     * 密码
     */
    @NotBlank(message = "不能为空")
    private String password;
    /**
     * 验证码Key
     */
    @NotBlank(message = "不能为空")
    private String captchaKey;
    /**
     * 验证码
     */
    @NotBlank(message = "不能为空")
    private String captchaCode;
}
