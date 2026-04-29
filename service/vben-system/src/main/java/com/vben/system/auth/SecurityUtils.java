package com.vben.system.auth;

import com.vben.common.exception.UnauthorizedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import java.security.Principal;

/**
 * 安全上下文工具类。
 * <p>
 * 统一封装当前登录用户的获取逻辑，避免业务代码直接操作 SecurityContextHolder。
 * 工具类内部尽量做容错处理:
 * 1. principal 类型不匹配时尽量兼容常见实现
 * 2. 获取不到当前用户时统一抛出未登录异常
 * <p>
 * 约定说明:
 * 当前系统以 userId 作为唯一身份标识。
 * username 仅作为展示或兼容历史逻辑使用，业务判断不要依赖 username 唯一性。
 * <p>
 * 对外建议优先使用:
 * 1. getUser() 获取当前登录用户
 * 2. getUserId() 获取当前登录用户 ID
 * 3. getUsername() 获取当前登录用户名
 */
final class SecurityUtils {
    private SecurityUtils() {
    }

    /**
     * 获取当前登录用户。
     *
     * @return 当前登录用户
     * @throws UnauthorizedException 未登录、上下文为空或 principal 无法识别
     */
    public static CurrentUserPrincipal getUser() {
        Authentication authentication;
        try {
            authentication = SecurityContextHolder.getContext().getAuthentication();
        } catch (Exception ex) {
            throw new UnauthorizedException("获取当前登录用户失败");
        }
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new UnauthorizedException("未登录或登录已过期");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CurrentUserPrincipal currentUser) {
            return currentUser;
        }
        if (principal instanceof UserDetails userDetails && StringUtils.hasText(userDetails.getUsername())) {
            return new CurrentUserPrincipal(null, userDetails.getUsername(), null);
        }
        if (principal instanceof Principal namedPrincipal && StringUtils.hasText(namedPrincipal.getName())) {
            return new CurrentUserPrincipal(null, namedPrincipal.getName(), null);
        }
        if (principal instanceof String username && StringUtils.hasText(username) && !"anonymousUser".equals(username)) {
            return new CurrentUserPrincipal(null, username, null);
        }
        throw new UnauthorizedException("未登录或登录已过期");
    }

    /**
     * 获取当前登录用户名。
     * <p>
     * 注意：用户名不是唯一标识，只建议用于页面展示或兼容老接口。
     */
    public static String getUsername() {
        return getUser().username();
    }

    /**
     * 获取当前登录用户 ID。
     * <p>
     * 当前系统中 userId 是唯一身份标识，业务代码应优先使用该方法。
     */
    public static Long getUserId() {
        Long userId = getUser().userId();
        if (userId == null) {
            throw new UnauthorizedException("当前登录用户缺少 userId 信息");
        }
        return userId;
    }

    /**
     * 获取当前登录会话 ID。
     */
    public static String getSessionId() {
        String sessionId = getUser().sessionId();
        if (!StringUtils.hasText(sessionId)) {
            throw new UnauthorizedException("当前登录用户缺少 sessionId 信息");
        }
        return sessionId;
    }
}
