package com.vben.system.dto.auth;

import lombok.Builder;
import lombok.Data;

/**
 * 登录/刷新令牌响应。
 */
@Data
@Builder
public class TokenResponse {
    /** 访问令牌 */
    private String accessToken;
    /** 刷新令牌 */
    private String refreshToken;
    /** Access Token 过期秒数 */
    private Long expiresIn;
}
