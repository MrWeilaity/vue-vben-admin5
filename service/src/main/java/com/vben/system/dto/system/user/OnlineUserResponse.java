package com.vben.system.dto.system.user;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OnlineUserResponse {
    private String sessionId;
    private Long userId;
    private String username;
    private String deptName;
    private String loginIp;
    private String loginAddress;
    private String browser;
    private String os;
    private String deviceType;
    private LocalDateTime loginTime;
    private LocalDateTime lastAccessTime;
    private LocalDateTime expiresAt;
    private Boolean current;
}
