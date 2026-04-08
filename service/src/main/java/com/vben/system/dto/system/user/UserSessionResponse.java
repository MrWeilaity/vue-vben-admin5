package com.vben.system.dto.system.user;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 在线设备会话响应。
 */
@Data
@Builder
public class UserSessionResponse {
    private String sessionId;
    private Long userId;
    private String loginIp;
    private String userAgent;
    private String deviceType;
    private LocalDateTime loginTime;
    private LocalDateTime lastAccessTime;
    private LocalDateTime expiresAt;
    private Boolean current;
}
