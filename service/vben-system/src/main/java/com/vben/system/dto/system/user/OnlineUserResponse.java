package com.vben.system.dto.system.user;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastAccessTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;
    private Boolean current;
}
