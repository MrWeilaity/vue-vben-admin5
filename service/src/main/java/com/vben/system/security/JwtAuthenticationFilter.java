package com.vben.system.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.vben.system.service.AuthSessionService;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

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
    private final PermissionCodeService permissionCodeService;
    private final AuthSessionService authSessionService;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return SecurityWhitelist.isPublic(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = tokenService.parse(token);
                String tokenType = claims.get("typ", String.class);
                if (!"access".equals(tokenType)) {
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

                String sessionId = claims.get("sid", String.class);
                if (!StringUtils.hasText(sessionId)) {
                    log.warn("JWT缺少sessionId，已忽略本次认证: userId={}, uri={}, method={}", userId, request.getRequestURI(), request.getMethod());
                    filterChain.doFilter(request, response);
                    return;
                }
                AuthSessionService.SessionRecord session = authSessionService.getSession(sessionId);
                if (session == null) {
                    log.warn("Redis中缺少登录会话，已拒绝本次认证: userId={}, sessionId={}", userId, sessionId);
                    filterChain.doFilter(request, response);
                    return;
                }
                if (!Objects.equals(userId, session.getUserId())) {
                    log.warn("JWT用户与Redis会话用户不一致，已拒绝本次认证: tokenUserId={}, sessionUserId={}, sessionId={}",
                            userId, session.getUserId(), sessionId);
                    filterChain.doFilter(request, response);
                    return;
                }
                authSessionService.touchSession(sessionId, Duration.ofSeconds(tokenService.getAccessExpireSeconds()));

                String username = session.getUsername();
                if (!StringUtils.hasText(username)) {
                    username = claims.get("uname", String.class);
                }
                if (!StringUtils.hasText(username)) {
                    username = String.valueOf(userId);
                }
                CurrentUserPrincipal principal = new CurrentUserPrincipal(userId, username, sessionId);
                List<SimpleGrantedAuthority> authorities = permissionCodeService.getAccessCodesByUserId(userId).stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, authorities);
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
