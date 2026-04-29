package com.vben.system.auth;

import org.springframework.security.core.AuthenticatedPrincipal;

import java.io.Serial;
import java.io.Serializable;

/**
 * 当前登录用户主体。
 * <p>
 * 这里保存的是当前请求已经完成鉴权后的最小用户信息，
 * 方便在 Controller / Service 中直接获取当前登录人。
 *
 * @param userId    当前登录用户 ID。
 * @param username  当前登录用户名，通常对应系统登录账号。
 * @param sessionId 当前登录会话 ID。
 */
public record CurrentUserPrincipal(Long userId, String username, String sessionId) implements AuthenticatedPrincipal, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 构造当前登录用户主体。
     *
     * @param userId    用户 ID，兼容场景下允许为空
     * @param username  用户名
     * @param sessionId 会话 ID
     */
    public CurrentUserPrincipal {
    }

    /**
     * Spring Security 默认使用该名称作为当前认证名。
     */
    @Override
    public String getName() {
        return username;
    }
}
