package com.vben.system.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 认证过滤器。
 * <p>
 * 负责从请求头中解析 Bearer Token，并在校验通过后把当前用户放入安全上下文。
 * 这里对异常和脏数据做了容错处理：只要 token 无效，就不写入认证信息，
 * 交给后续 Spring Security 按未登录处理，避免把底层解析异常直接暴露给业务层。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenService tokenService;
    private final StringRedisTemplate redisTemplate;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return SecurityWhitelist.isPublic(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = tokenService.parse(token);
                String jti = claims.getId();
                // 命中黑名单说明 token 已经被主动注销，直接按未登录处理。
                if (redisTemplate.hasKey("auth:blacklist:" + jti)) {
                    filterChain.doFilter(request, response);
                    return;
                }
                String tokenType = claims.get("typ", String.class);
                // 兼容历史 access token：旧 token 不带 typ，允许在过渡期继续使用
                if (StringUtils.hasText(tokenType) && !"access".equals(tokenType)) {
                    filterChain.doFilter(request, response);
                    return;
                }

                String subject = claims.getSubject();
                if (!StringUtils.hasText(subject)) {
                    log.warn("JWT缺少subject，已忽略本次认证: uri={}, method={}", request.getRequestURI(), request.getMethod());
                    filterChain.doFilter(request, response);
                    return;
                }

                Long userId;
                try {
                    userId = Long.valueOf(subject);
                } catch (NumberFormatException ex) {
                    log.warn("JWT subject不是合法的用户ID: subject={}, uri={}, method={}", subject, request.getRequestURI(), request.getMethod());
                    filterChain.doFilter(request, response);
                    return;
                }

                String username = claims.get("uname", String.class);
                if (!StringUtils.hasText(username)) {
                    // 如果 token 中没有用户名，退化为使用 userId，至少保证当前请求可识别出调用人。
                    username = String.valueOf(userId);
                }
                Integer tokenVersion = claims.get("ver", Integer.class);
                if (tokenVersion == null) {
                    log.warn("JWT缺少版本号，已忽略本次认证: userId={}, uri={}, method={}", userId, request.getRequestURI(), request.getMethod());
                    filterChain.doFilter(request, response);
                    return;
                }
                String versionInRedis = redisTemplate.opsForValue().get("auth:token:version:" + userId);
                if (versionInRedis != null) {
                    try {
                        if (tokenVersion != Integer.parseInt(versionInRedis)) {
                            filterChain.doFilter(request, response);
                            return;
                        }
                    } catch (NumberFormatException ex) {
                        log.warn("Redis中的token版本号不是数字，已拒绝本次认证: userId={}, versionInRedis={}", userId, versionInRedis);
                        filterChain.doFilter(request, response);
                        return;
                    }
                }

                CurrentUserPrincipal principal = new CurrentUserPrincipal(userId, username);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal, null, List.of());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception ex) {
                log.error(
                    "JWT解析失败: uri={}, method={}, message={}",
                    request.getRequestURI(),
                    request.getMethod(),
                    ex.getMessage(),
                    ex
                );
            }
        }
        filterChain.doFilter(request, response);
    }
}
