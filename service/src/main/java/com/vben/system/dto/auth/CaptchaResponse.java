package com.vben.system.dto.auth;

import lombok.Builder;
import lombok.Data;

/**
 * 验证码响应。
 */
@Data
@Builder
public class CaptchaResponse {
    /** 验证码 key */
    private String captchaKey;
    /** 验证码值（开发环境明文返回，生产建议返回图片或关闭该字段） */
    private String captchaCode;
    /** 过期时间（秒） */
    private int expireSeconds;
}
