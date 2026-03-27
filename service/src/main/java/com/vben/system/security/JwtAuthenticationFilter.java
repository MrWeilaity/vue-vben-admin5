package com.vben.system.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JwtAuthenticationFilter 组件说明。
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService tokenService;
    private final StringRedisTemplate redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
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
                String jti = claims.getId();
                if (Boolean.TRUE.equals(redisTemplate.hasKey("auth:blacklist:" + jti))) {
                    filterChain.doFilter(request, response);
                    return;
                }
                String userId = claims.getSubject();
                Integer tokenVersion = claims.get("ver", Integer.class);
                String versionInRedis = redisTemplate.opsForValue().get("auth:token:version:" + userId);
                if (versionInRedis != null && tokenVersion != Integer.parseInt(versionInRedis)) {
                    filterChain.doFilter(request, response);
                    return;
                }

                User principal = new User(userId, "", List.of());
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception ignored) {
            }
        }
        filterChain.doFilter(request, response);
    }
}
