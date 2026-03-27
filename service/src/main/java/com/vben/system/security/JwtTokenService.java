package com.vben.system.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

/**
 * JwtTokenService 组件说明。
 */
@Service
public class JwtTokenService {
    private final SecretKey key;
    private final String issuer;
    private final long accessExpireSeconds;
    private final long refreshExpireSeconds;
    private final StringRedisTemplate redisTemplate;

    public JwtTokenService(
        @Value("${security.jwt.secret}") String secret,
        @Value("${security.jwt.issuer}") String issuer,
        @Value("${security.jwt.access-token-expire-seconds}") long accessExpireSeconds,
        @Value("${security.jwt.refresh-token-expire-seconds}") long refreshExpireSeconds,
        StringRedisTemplate redisTemplate
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.accessExpireSeconds = accessExpireSeconds;
        this.refreshExpireSeconds = refreshExpireSeconds;
        this.redisTemplate = redisTemplate;
    }

    public String createAccessToken(Long userId, int version) {
        String jti = UUID.randomUUID().toString();
        Date now = new Date();
        Date exp = new Date(now.getTime() + accessExpireSeconds * 1000);
        return Jwts.builder()
            .issuer(issuer)
            .subject(String.valueOf(userId))
            .id(jti)
            .claim("typ", "access")
            .claim("ver", version)
            .issuedAt(now)
            .expiration(exp)
            .signWith(key)
            .compact();
    }

    public String createRefreshToken(Long userId, int version) {
        String jti = UUID.randomUUID().toString();
        Date now = new Date();
        Date exp = new Date(now.getTime() + refreshExpireSeconds * 1000);
        String token = Jwts.builder()
            .issuer(issuer)
            .subject(String.valueOf(userId))
            .id(jti)
            .claim("typ", "refresh")
            .claim("ver", version)
            .issuedAt(now)
            .expiration(exp)
            .signWith(key)
            .compact();
        redisTemplate.opsForValue().set("auth:refresh:" + token, String.valueOf(userId), Duration.ofSeconds(refreshExpireSeconds));
        return token;
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public boolean existsRefreshToken(String refreshToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("auth:refresh:" + refreshToken));
    }

    public void removeRefreshToken(String refreshToken) {
        redisTemplate.delete("auth:refresh:" + refreshToken);
    }

    public long getAccessExpireSeconds() {
        return accessExpireSeconds;
    }
}
