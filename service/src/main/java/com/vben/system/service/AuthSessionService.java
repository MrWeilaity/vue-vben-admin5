package com.vben.system.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vben.system.common.exception.BadRequestException;
import com.vben.system.dto.system.user.UserSessionResponse;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 统一管理登录会话、refresh token 绑定关系和在线设备列表。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthSessionService {
    private static final String SESSION_KEY_PREFIX = "auth:session:";
    private static final String USER_SESSIONS_KEY_PREFIX = "auth:user:sessions:";
    private static final String REFRESH_TOKEN_KEY_PREFIX = "auth:refresh:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public SessionRecord createSession(
            Long userId,
            String username,
            String loginIp,
            String userAgent,
            Duration accessTtl,
            Duration refreshTtl
    ) {
        LocalDateTime now = LocalDateTime.now();
        SessionRecord session = SessionRecord.builder()
                .sessionId(UUID.randomUUID().toString().replace("-", ""))
                .userId(userId)
                .username(username)
                .loginIp(loginIp)
                .userAgent(userAgent)
                .deviceType(resolveDeviceType(userAgent))
                .loginTime(now)
                .lastAccessTime(now)
                .accessExpireAt(now.plusSeconds(accessTtl.toSeconds()))
                .refreshExpireAt(now.plusSeconds(refreshTtl.toSeconds()))
                .build();
        saveSession(session, refreshTtl);
        redisTemplate.opsForSet().add(userSessionsKey(userId), session.getSessionId());
        return session;
    }

    public SessionRecord getSession(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            return null;
        }
        String raw = redisTemplate.opsForValue().get(sessionKey(sessionId));
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        try {
            return objectMapper.readValue(raw, SessionRecord.class);
        } catch (Exception ex) {
            log.warn("读取登录会话失败，已删除损坏数据: sessionId={}", sessionId, ex);
            redisTemplate.delete(sessionKey(sessionId));
            return null;
        }
    }

    public List<UserSessionResponse> listUserSessions(Long userId, String currentSessionId) {
        Set<String> sessionIds = redisTemplate.opsForSet().members(userSessionsKey(userId));
        if (sessionIds == null || sessionIds.isEmpty()) {
            return List.of();
        }
        List<UserSessionResponse> responses = new ArrayList<>();
        for (String sessionId : new LinkedHashSet<>(sessionIds)) {
            SessionRecord session = getSession(sessionId);
            if (session == null) {
                redisTemplate.opsForSet().remove(userSessionsKey(userId), sessionId);
                continue;
            }
            if (!Objects.equals(userId, session.getUserId())) {
                deleteSession(sessionId);
                continue;
            }
            responses.add(UserSessionResponse.builder()
                    .sessionId(session.getSessionId())
                    .userId(session.getUserId())
                    .loginIp(session.getLoginIp())
                    .userAgent(session.getUserAgent())
                    .deviceType(session.getDeviceType())
                    .loginTime(session.getLoginTime())
                    .lastAccessTime(session.getLastAccessTime())
                    .expiresAt(session.getRefreshExpireAt())
                    .current(StringUtils.hasText(currentSessionId) && currentSessionId.equals(session.getSessionId()))
                    .build());
        }
        return responses.stream()
                .sorted(Comparator.comparing(UserSessionResponse::getLoginTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public boolean isRefreshTokenBound(String refreshToken, String sessionId) {
        if (!StringUtils.hasText(refreshToken) || !StringUtils.hasText(sessionId)) {
            return false;
        }
        return sessionId.equals(redisTemplate.opsForValue().get(refreshTokenKey(refreshToken)));
    }

    public void bindRefreshToken(String sessionId, String refreshToken, Duration refreshTtl) {
        SessionRecord session = requireSession(sessionId);
        String oldRefreshToken = session.getRefreshToken();
        if (StringUtils.hasText(oldRefreshToken) && !oldRefreshToken.equals(refreshToken)) {
            redisTemplate.delete(refreshTokenKey(oldRefreshToken));
        }
        session.setRefreshToken(refreshToken);
        session.setRefreshExpireAt(LocalDateTime.now().plusSeconds(refreshTtl.toSeconds()));
        saveSession(session, refreshTtl);
        redisTemplate.opsForValue().set(refreshTokenKey(refreshToken), sessionId, refreshTtl);
    }

    public void touchSession(String sessionId, Duration accessTtl) {
        SessionRecord session = getSession(sessionId);
        if (session == null) {
            return;
        }
        session.setLastAccessTime(LocalDateTime.now());
        session.setAccessExpireAt(LocalDateTime.now().plusSeconds(accessTtl.toSeconds()));
        saveSession(session, currentSessionTtl(sessionId, session));
    }

    public void refreshSession(String sessionId, String newRefreshToken, Duration accessTtl, Duration refreshTtl) {
        SessionRecord session = requireSession(sessionId);
        String oldRefreshToken = session.getRefreshToken();
        if (StringUtils.hasText(oldRefreshToken) && !oldRefreshToken.equals(newRefreshToken)) {
            redisTemplate.delete(refreshTokenKey(oldRefreshToken));
        }
        LocalDateTime now = LocalDateTime.now();
        session.setLastAccessTime(now);
        session.setAccessExpireAt(now.plusSeconds(accessTtl.toSeconds()));
        session.setRefreshExpireAt(now.plusSeconds(refreshTtl.toSeconds()));
        session.setRefreshToken(newRefreshToken);
        saveSession(session, refreshTtl);
        redisTemplate.opsForValue().set(refreshTokenKey(newRefreshToken), sessionId, refreshTtl);
    }

    public void deleteSession(String sessionId) {
        SessionRecord session = getSession(sessionId);
        if (session != null) {
            if (StringUtils.hasText(session.getRefreshToken())) {
                redisTemplate.delete(refreshTokenKey(session.getRefreshToken()));
            }
            redisTemplate.opsForSet().remove(userSessionsKey(session.getUserId()), sessionId);
        }
        redisTemplate.delete(sessionKey(sessionId));
    }

    public void deleteUserSessions(Long userId) {
        Set<String> sessionIds = redisTemplate.opsForSet().members(userSessionsKey(userId));
        if (sessionIds != null) {
            for (String sessionId : sessionIds) {
                deleteSession(sessionId);
            }
        }
        redisTemplate.delete(userSessionsKey(userId));
    }

    public void deleteUserSession(Long userId, String sessionId) {
        SessionRecord session = requireSession(sessionId);
        if (!Objects.equals(userId, session.getUserId())) {
            throw new BadRequestException("会话不属于当前用户");
        }
        deleteSession(sessionId);
    }

    public SessionRecord requireSession(String sessionId) {
        SessionRecord session = getSession(sessionId);
        if (session == null) {
            throw new BadRequestException("登录会话不存在或已失效");
        }
        return session;
    }

    private void saveSession(SessionRecord session, Duration ttl) {
        try {
            ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
            valueOps.set(sessionKey(session.getSessionId()), objectMapper.writeValueAsString(session), ttl);
        } catch (Exception ex) {
            throw new IllegalStateException("保存登录会话失败", ex);
        }
    }

    private Duration currentSessionTtl(String sessionId, SessionRecord session) {
        Long ttlSeconds = redisTemplate.getExpire(sessionKey(sessionId), TimeUnit.SECONDS);
        if (ttlSeconds == null || ttlSeconds <= 0) {
            if (session.getRefreshExpireAt() == null) {
                return Duration.ofDays(14);
            }
            long seconds = session.getRefreshExpireAt().atZone(ZoneId.systemDefault()).toEpochSecond()
                    - LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond();
            return Duration.ofSeconds(Math.max(seconds, 1));
        }
        return Duration.ofSeconds(ttlSeconds);
    }

    private String resolveDeviceType(String userAgent) {
        if (!StringUtils.hasText(userAgent)) {
            return "Unknown";
        }
        String value = userAgent.toLowerCase();
        if (value.contains("iphone") || value.contains("ipad") || value.contains("android") || value.contains("mobile")) {
            return "Mobile";
        }
        if (value.contains("windows") || value.contains("macintosh") || value.contains("linux")) {
            return "Desktop";
        }
        return "Unknown";
    }

    private String sessionKey(String sessionId) {
        return SESSION_KEY_PREFIX + sessionId;
    }

    private String userSessionsKey(Long userId) {
        return USER_SESSIONS_KEY_PREFIX + userId;
    }

    private String refreshTokenKey(String refreshToken) {
        return REFRESH_TOKEN_KEY_PREFIX + refreshToken;
    }

    @Data
    @Builder
    public static class SessionRecord {
        private String sessionId;
        private Long userId;
        private String username;
        private String loginIp;
        private String userAgent;
        private String deviceType;
        private LocalDateTime loginTime;
        private LocalDateTime lastAccessTime;
        private LocalDateTime accessExpireAt;
        private LocalDateTime refreshExpireAt;
        private String refreshToken;
    }
}
