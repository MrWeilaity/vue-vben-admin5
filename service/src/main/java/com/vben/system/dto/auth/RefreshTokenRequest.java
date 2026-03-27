package com.vben.system.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新令牌请求参数。
 */
@Data
public class RefreshTokenRequest {
    /** 刷新令牌 */
    @NotBlank
    private String refreshToken;
}
