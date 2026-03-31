package com.vben.system.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import java.util.List;

/**
 * 安全白名单。
 * <p>
 * 统一维护匿名可访问接口，避免授权配置与 JWT 过滤器规则漂移。
 */
public final class SecurityWhitelist {
    private static final PathPatternRequestMatcher.Builder PATHS = PathPatternRequestMatcher.withDefaults();

    private static final List<RequestMatcher> PUBLIC_MATCHERS = List.of(
        PATHS.matcher(HttpMethod.POST, "/api/auth/login"),
        PATHS.matcher(HttpMethod.POST, "/api/auth/refresh"),
        PATHS.matcher(HttpMethod.POST, "/api/auth/logout"),
        PATHS.matcher(HttpMethod.GET, "/api/auth/captcha"),
        PATHS.matcher("/v3/api-docs/**"),
        PATHS.matcher("/swagger-ui/**"),
        PATHS.matcher("/swagger-ui.html"),
        PATHS.matcher("/scalar/**"),
        PATHS.matcher("/actuator/health")
    );

    private static final RequestMatcher PUBLIC_ENDPOINTS = new OrRequestMatcher(PUBLIC_MATCHERS);

    private SecurityWhitelist() {
    }

    public static RequestMatcher[] publicMatchers() {
        return PUBLIC_MATCHERS.toArray(RequestMatcher[]::new);
    }

    public static boolean isPublic(HttpServletRequest request) {
        return PUBLIC_ENDPOINTS.matches(request);
    }
}
