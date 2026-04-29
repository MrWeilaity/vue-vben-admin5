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
    /** 验证码图片 Base64 数据（不含 data:image 前缀） */
    private String captchaImageBase64;
    /** 过期时间（秒） */
    private int expireSeconds;
}
