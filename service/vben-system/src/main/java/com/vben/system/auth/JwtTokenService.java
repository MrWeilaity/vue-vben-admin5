package com.vben.system.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JwtTokenService 组件说明。
 */
@Service
public class JwtTokenService {
    private final SecretKey key;
    private final String issuer;
    @Getter
    private final long accessExpireSeconds;
    @Getter
    private final long refreshExpireSeconds;

    public JwtTokenService(
        @Value("${security.jwt.secret}") String secret,
        @Value("${security.jwt.issuer}") String issuer,
        @Value("${security.jwt.access-token-expire-seconds}") long accessExpireSeconds,
        @Value("${security.jwt.refresh-token-expire-seconds}") long refreshExpireSeconds
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.accessExpireSeconds = accessExpireSeconds;
        this.refreshExpireSeconds = refreshExpireSeconds;
    }

    public String createAccessToken(Long userId, String sessionId) {
        return createAccessToken(userId, sessionId, null);
    }

    public String createAccessToken(Long userId, String sessionId, String username) {
        String jti = UUID.randomUUID().toString();
        Date now = new Date();
        Date exp = new Date(now.getTime() + accessExpireSeconds * 1000);
        var builder = Jwts.builder()
            .issuer(issuer)
            .subject(String.valueOf(userId))
            .id(jti)
            .claim("sid", sessionId)
            .claim("typ", "access")
            .issuedAt(now)
            .expiration(exp)
            .signWith(key);
        if (StringUtils.hasText(username)) {
            builder.claim("uname", username);
        }
        return builder.compact();
    }

    public String createRefreshToken(Long userId, String sessionId) {
        return createRefreshToken(userId, sessionId, null);
    }

    public String createRefreshToken(Long userId, String sessionId, String username) {
        String jti = UUID.randomUUID().toString();
        Date now = new Date();
        Date exp = new Date(now.getTime() + refreshExpireSeconds * 1000);
        var builder = Jwts.builder()
            .issuer(issuer)
            .subject(String.valueOf(userId))
            .id(jti)
            .claim("sid", sessionId)
            .claim("typ", "refresh")
            .issuedAt(now)
            .expiration(exp)
            .signWith(key);
        if (StringUtils.hasText(username)) {
            builder.claim("uname", username);
        }
        return builder.compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

}
