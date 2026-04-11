package com.vben.system.service;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vben.system.common.exception.BadRequestException;
import com.vben.system.common.exception.ForbiddenException;
import com.vben.system.common.exception.RefreshTokenExpiredException;
import com.vben.system.dto.auth.LoginRequest;
import com.vben.system.dto.auth.TokenResponse;
import com.vben.system.dto.system.user.UserSessionResponse;
import com.vben.system.entity.SysUser;
import com.vben.system.mapper.SysUserMapper;
import com.vben.system.security.JwtTokenService;
import com.vben.system.security.LoginUserService;
import com.vben.system.security.PermissionCodeService;
import com.vben.system.service.system.ISysLoginLogService;
import com.vben.system.service.system.IpLocationResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;

/**
 * AuthService 组件说明。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private static final String CAPTCHA_CHAR_POOL = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int CAPTCHA_LENGTH = 4;
    private static final int CAPTCHA_WIDTH = 130;
    private static final int CAPTCHA_HEIGHT = 48;
    private static final int CAPTCHA_INTERFERE_COUNT = 20;

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService tokenService;
    private final StringRedisTemplate redisTemplate;
    private final LoginUserService loginUserService;
    private final PermissionCodeService permissionCodeService;
    private final AuthSessionService authSessionService;
    private final ISysLoginLogService loginLogService;
    private final IpLocationResolver ipLocationResolver;

    public TokenResponse login(LoginRequest request, String ip, String userAgent) {
        String captchaRedisKey = null;
        String username = request.getUsername();
        String operationMsg = "登录成功";
        boolean success = false;
        try {
            String failKey = "auth:fail:" + request.getUsername();
            String failedCount = redisTemplate.opsForValue().get(failKey);
            if (failedCount != null && Integer.parseInt(failedCount) >= 5) {
                throw new ForbiddenException("登录失败次数过多，请稍后再试");
            }

            if (!StringUtils.hasText(request.getCaptchaKey()) || !StringUtils.hasText(request.getCaptchaCode())) {
                throw new BadRequestException("验证码或验证码Key不能为空");
            }

            captchaRedisKey = "auth:captcha:" + request.getCaptchaKey();
            String captcha = redisTemplate.opsForValue().get(captchaRedisKey);
            if (captcha == null || !captcha.equalsIgnoreCase(request.getCaptchaCode())) {
                throw new BadRequestException("验证码错误");
            }

            SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername()));
            if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                redisTemplate.opsForValue().increment(failKey);
                redisTemplate.expire(failKey, Duration.ofMinutes(15));
                throw new ForbiddenException("账号或密码错误");
            }
            if (user.getStatus() == null || user.getStatus() != 1) {
                throw new ForbiddenException("账号已被禁用，请联系管理员");
            }

            redisTemplate.delete(failKey);
            Duration accessTtl = Duration.ofSeconds(tokenService.getAccessExpireSeconds());
            Duration refreshTtl = Duration.ofSeconds(tokenService.getRefreshExpireSeconds());
            String loginAddress = resolveLoginAddress(ip);
            UserAgentInfo userAgentInfo = parseUserAgent(userAgent);
            AuthSessionService.SessionRecord session = authSessionService.createSession(
                    user.getId(),
                    user.getUsername(),
                    ip,
                    loginAddress,
                    userAgent,
                    userAgentInfo.browser(),
                    userAgentInfo.os(),
                    accessTtl,
                    refreshTtl
            );
            String accessToken = tokenService.createAccessToken(user.getId(), session.getSessionId(), user.getUsername());
            String refreshToken = tokenService.createRefreshToken(user.getId(), session.getSessionId(), user.getUsername());
            authSessionService.bindRefreshToken(session.getSessionId(), refreshToken, refreshTtl);
            user.setLastLoginIp(ip);
            user.setLastLoginTime(java.time.LocalDateTime.now());
            userMapper.updateById(user);
            success = true;
            return TokenResponse.builder().accessToken(accessToken).refreshToken(refreshToken).expiresIn(tokenService.getAccessExpireSeconds()).build();
        } catch (Exception ex) {
            operationMsg = ex.getMessage();
            throw ex;
        } finally {
            try {
                loginLogService.record(username, ip, userAgent, success, operationMsg);
            } catch (Exception logException) {
                log.warn("Failed to enqueue login log, username={}, ip={}, success={}", username, ip, success, logException);
            }
            if (captchaRedisKey != null) {
                redisTemplate.delete(captchaRedisKey);
            }
        }
    }

    public TokenResponse refresh(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            throw new BadRequestException("refresh token 不能为空");
        }
        var claims = parseRefreshClaims(refreshToken);
        String tokenType = claims.get("typ", String.class);
        if (!"refresh".equals(tokenType)) {
            throw new BadRequestException("token 类型错误");
        }
        Long userId = Long.valueOf(claims.getSubject());
        String sessionId = claims.get("sid", String.class);
        if (!StringUtils.hasText(sessionId)) {
            throw new RefreshTokenExpiredException("refresh token 已失效");
        }
        if (!authSessionService.isRefreshTokenBound(refreshToken, sessionId)) {
            throw new RefreshTokenExpiredException("refresh token 已失效");
        }
        AuthSessionService.SessionRecord session = authSessionService.getSession(sessionId);
        if (session == null || !userId.equals(session.getUserId())) {
            throw new RefreshTokenExpiredException("refresh token 已失效");
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            authSessionService.deleteUserSessions(userId);
            throw new ForbiddenException("用户不存在或已被删除");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            authSessionService.deleteUserSessions(userId);
            throw new ForbiddenException("账号已被禁用，请联系管理员");
        }
        String accessToken = tokenService.createAccessToken(userId, sessionId, user.getUsername());
        String newRefreshToken = tokenService.createRefreshToken(userId, sessionId, user.getUsername());
        authSessionService.refreshSession(
                sessionId,
                newRefreshToken,
                Duration.ofSeconds(tokenService.getAccessExpireSeconds()),
                Duration.ofSeconds(tokenService.getRefreshExpireSeconds())
        );
        return TokenResponse.builder().accessToken(accessToken).refreshToken(newRefreshToken).expiresIn(tokenService.getAccessExpireSeconds()).build();
    }

    public void logout(String accessToken, String refreshToken) {
        try {
            var claims = tokenService.parse(accessToken);
            String sessionId = claims.get("sid", String.class);
            if (StringUtils.hasText(sessionId)) {
                authSessionService.deleteSession(sessionId);
                return;
            }
        } catch (Exception ignored) {
        }
        if (StringUtils.hasText(refreshToken)) {
            try {
                String sessionId = tokenService.parse(refreshToken).get("sid", String.class);
                if (authSessionService.isRefreshTokenBound(refreshToken, sessionId) && StringUtils.hasText(sessionId)) {
                    authSessionService.deleteSession(sessionId);
                }
            } catch (Exception ignored) {
            }
        }
    }

    public void forceOffline(Long userId) {
        authSessionService.deleteUserSessions(userId);
    }

    public void forceOffline(Long userId, String sessionId) {
        authSessionService.deleteUserSession(userId, sessionId);
    }

    public List<UserSessionResponse> listUserSessions(Long userId, String currentSessionId) {
        return authSessionService.listUserSessions(userId, currentSessionId);
    }

    /**
     * 根据用户 ID 获取权限码。
     * <p>
     * 用户唯一身份以 userId 为准，不依赖 username 做权限查询。
     *
     * @return 权限码列表；如果用户不存在或没有分配角色/权限，则返回空列表
     */
    public List<String> getAccessCodes() {
        return permissionCodeService.getAccessCodesByUserId(loginUserService.getCurrentUserId());
    }

    public List<String> getAccessCodesByUserId(Long userId) {
        return permissionCodeService.getAccessCodesByUserId(userId);
    }

    public CaptchaPayload generateCaptcha(String captchaKey, Duration ttl) {
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(
                CAPTCHA_WIDTH,
                CAPTCHA_HEIGHT,
                CAPTCHA_LENGTH,
                CAPTCHA_INTERFERE_COUNT
        );
        lineCaptcha.setGenerator(new RandomGenerator(CAPTCHA_CHAR_POOL, CAPTCHA_LENGTH));
        lineCaptcha.createCode();

        String code = lineCaptcha.getCode();
        redisTemplate.opsForValue().set("auth:captcha:" + captchaKey, code, ttl);
        return new CaptchaPayload(lineCaptcha.getImageBase64Data(), ttl.toSeconds());
    }


    private String resolveLoginAddress(String ip) {
        if (!StringUtils.hasText(ip)) {
            return "未知";
        }
        if (isInnerIp(ip)) {
            return "内网IP";
        }
        String addr = ipLocationResolver.resolve(ip);
        return StringUtils.hasText(addr) ? addr : "未知";
    }

    private boolean isInnerIp(String ip) {
        if (ip.startsWith("172.")) {
            String[] segments = ip.split("\\.");
            if (segments.length >= 2) {
                try {
                    int second = Integer.parseInt(segments[1]);
                    if (second >= 16 && second <= 31) {
                        return true;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return "127.0.0.1".equals(ip)
                || "localhost".equalsIgnoreCase(ip)
                || ip.startsWith("192.168.")
                || ip.startsWith("10.")
                || ip.startsWith("169.254.");
    }

    private UserAgentInfo parseUserAgent(String userAgent) {
        eu.bitwalker.useragentutils.UserAgent parsed = eu.bitwalker.useragentutils.UserAgent.parseUserAgentString(userAgent);
        String browser = parsed.getBrowser() == null ? "Unknown" : parsed.getBrowser().getGroup().getName();
        String os = parsed.getOperatingSystem() == null ? "Unknown" : parsed.getOperatingSystem().getGroup().getName();
        return new UserAgentInfo(browser, os);
    }

    public record CaptchaPayload(String captchaImageBase64, long expireSeconds) {
    }

    private record UserAgentInfo(String browser, String os) {
    }

    private io.jsonwebtoken.Claims parseRefreshClaims(String refreshToken) {
        try {
            return tokenService.parse(refreshToken);
        } catch (Exception ex) {
            throw new RefreshTokenExpiredException("refresh token 已失效");
        }
    }
}
