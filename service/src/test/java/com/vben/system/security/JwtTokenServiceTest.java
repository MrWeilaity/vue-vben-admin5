package com.vben.system.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtTokenServiceTest {

    @Test
    void shouldCreateAccessAndRefreshTokenWithUserIdAndSessionId() {
        JwtTokenService tokenService = new JwtTokenService(
                "please-change-to-very-long-key-at-least-256-bits",
                "vben-service",
                1800,
                1209600
        );

        String accessToken = tokenService.createAccessToken(101L, "session-abc", "alice");
        String refreshToken = tokenService.createRefreshToken(101L, "session-abc", "alice");

        Claims accessClaims = tokenService.parse(accessToken);
        Claims refreshClaims = tokenService.parse(refreshToken);

        assertEquals("101", accessClaims.getSubject());
        assertEquals("session-abc", accessClaims.get("sid", String.class));
        assertEquals("access", accessClaims.get("typ", String.class));

        assertEquals("101", refreshClaims.getSubject());
        assertEquals("session-abc", refreshClaims.get("sid", String.class));
        assertEquals("refresh", refreshClaims.get("typ", String.class));
    }
}
