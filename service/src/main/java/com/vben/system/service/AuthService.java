package com.vben.system.service;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vben.system.common.exception.BadRequestException;
import com.vben.system.common.exception.ForbiddenException;
import com.vben.system.dto.auth.LoginRequest;
import com.vben.system.dto.auth.TokenResponse;
import com.vben.system.entity.SysUser;
import com.vben.system.mapper.SysUserMapper;
import com.vben.system.security.JwtTokenService;
import com.vben.system.security.LoginUserService;
import com.vben.system.security.PermissionCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * AuthService 组件说明。
 */
@Service
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

    public TokenResponse login(LoginRequest request, String ip) {
        String captchaRedisKey = null;
        if (StringUtils.hasText(request.getCaptchaKey())) {
            captchaRedisKey = "auth:captcha:" + request.getCaptchaKey();
        }
        try {
            String failKey = "auth:fail:" + request.getUsername();
            String failedCount = redisTemplate.opsForValue().get(failKey);
            if (failedCount != null && Integer.parseInt(failedCount) >= 5) {
                throw new ForbiddenException("登录失败次数过多，请稍后再试");
            }

            if (StringUtils.hasText(request.getCaptchaKey()) || StringUtils.hasText(request.getCaptchaCode())) {
                String captcha = redisTemplate.opsForValue().get("auth:captcha:" + request.getCaptchaKey());
                if (captcha == null || !captcha.equalsIgnoreCase(request.getCaptchaCode())) {
                    throw new BadRequestException("验证码错误");
                }
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
            String versionKey = "auth:token:version:" + user.getId();
            if (!redisTemplate.hasKey(versionKey)) {
                redisTemplate.opsForValue().set(versionKey, "1");
            }
            int version = Integer.parseInt(Objects.requireNonNull(redisTemplate.opsForValue().get(versionKey)));
            String accessToken = tokenService.createAccessToken(user.getId(), version, user.getUsername());
            String refreshToken = tokenService.createRefreshToken(user.getId(), version, user.getUsername());
            redisTemplate.opsForHash().put("auth:session:" + user.getId(), "loginIp", ip);
            return TokenResponse.builder().accessToken(accessToken).refreshToken(refreshToken).expiresIn(tokenService.getAccessExpireSeconds()).build();
        } finally {
            if (captchaRedisKey != null) {
                redisTemplate.delete(captchaRedisKey);
            }
        }
    }

    public TokenResponse refresh(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            throw new BadRequestException("refresh token 不能为空");
        }
        if (!tokenService.existsRefreshToken(refreshToken)) {
            throw new ForbiddenException("refresh token 已失效");
        }
        var claims = tokenService.parse(refreshToken);
        String tokenType = claims.get("typ", String.class);
        // 兼容历史 refresh token：旧 token 不带 typ，允许在过渡期继续使用
        if (StringUtils.hasText(tokenType) && !"refresh".equals(tokenType)) {
            throw new BadRequestException("token 类型错误");
        }
        Long userId = Long.valueOf(claims.getSubject());
        int version = claims.get("ver", Integer.class);
        String username = claims.get("uname", String.class);
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new ForbiddenException("用户不存在或已被删除");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new ForbiddenException("账号已被禁用，请联系管理员");
        }
        if (!StringUtils.hasText(username)) {
            username = user.getUsername();
        }
        String accessToken = tokenService.createAccessToken(userId, version, username);
        String newRefreshToken = tokenService.createRefreshToken(userId, version, username);
        tokenService.removeRefreshToken(refreshToken);
        return TokenResponse.builder().accessToken(accessToken).refreshToken(newRefreshToken).expiresIn(tokenService.getAccessExpireSeconds()).build();
    }

    public void logout(String accessToken, String refreshToken) {
        try {
            var claims = tokenService.parse(accessToken);
            redisTemplate.opsForValue().set("auth:blacklist:" + claims.getId(), "1", Duration.ofHours(1));
        } catch (Exception ignored) {
        }
        if (StringUtils.hasText(refreshToken)) {
            tokenService.removeRefreshToken(refreshToken);
        }
    }

    public void forceOffline(Long userId) {
        String versionKey = "auth:token:version:" + userId;
        String currentVersion = redisTemplate.opsForValue().get(versionKey);
        int version = Integer.parseInt(currentVersion == null ? "1" : currentVersion);
        redisTemplate.opsForValue().set(versionKey, String.valueOf(version + 1));
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

    public record CaptchaPayload(String captchaImageBase64, long expireSeconds) {
    }
}
