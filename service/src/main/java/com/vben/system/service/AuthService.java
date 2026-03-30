package com.vben.system.service;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vben.system.dto.auth.LoginRequest;
import com.vben.system.dto.auth.TokenResponse;
import com.vben.system.entity.SysMenu;
import com.vben.system.entity.SysRoleMenu;
import com.vben.system.entity.SysUser;
import com.vben.system.entity.SysUserRole;
import com.vben.system.mapper.SysMenuMapper;
import com.vben.system.mapper.SysRoleMenuMapper;
import com.vben.system.mapper.SysUserMapper;
import com.vben.system.mapper.SysUserRoleMapper;
import com.vben.system.security.JwtTokenService;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class AuthService {
    private static final String CAPTCHA_CHAR_POOL = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int CAPTCHA_LENGTH = 4;
    private static final int CAPTCHA_WIDTH = 130;
    private static final int CAPTCHA_HEIGHT = 48;
    private static final int CAPTCHA_INTERFERE_COUNT = 20;


    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysMenuMapper menuMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService tokenService;
    private final StringRedisTemplate redisTemplate;

    public TokenResponse login(LoginRequest request, String ip) {
        String captchaRedisKey = null;
        if (StringUtils.hasText(request.getCaptchaKey())) {
            captchaRedisKey = "auth:captcha:" + request.getCaptchaKey();
        }
        try {
            String failKey = "auth:fail:" + request.getUsername();
            String failedCount = redisTemplate.opsForValue().get(failKey);
            if (failedCount != null && Integer.parseInt(failedCount) >= 5) {
                throw new RuntimeException("登录失败次数过多，请稍后再试");
            }

            if (StringUtils.hasText(request.getCaptchaKey()) || StringUtils.hasText(request.getCaptchaCode())) {
                String captcha = redisTemplate.opsForValue().get("auth:captcha:" + request.getCaptchaKey());
                if (captcha == null || !captcha.equalsIgnoreCase(request.getCaptchaCode())) {
                    throw new RuntimeException("验证码错误");
                }
            }

            SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername()));
            if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                redisTemplate.opsForValue().increment(failKey);
                redisTemplate.expire(failKey, Duration.ofMinutes(15));
                throw new RuntimeException("账号或密码错误");
            }

            redisTemplate.delete(failKey);
            String versionKey = "auth:token:version:" + user.getId();
            if (Boolean.FALSE.equals(redisTemplate.hasKey(versionKey))) {
                redisTemplate.opsForValue().set(versionKey, "1");
            }
            int version = Integer.parseInt(redisTemplate.opsForValue().get(versionKey));
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
            throw new RuntimeException("refresh token 不能为空");
        }
        if (!tokenService.existsRefreshToken(refreshToken)) {
            throw new RuntimeException("refresh token 已失效");
        }
        var claims = tokenService.parse(refreshToken);
        String tokenType = claims.get("typ", String.class);
        // 兼容历史 refresh token：旧 token 不带 typ，允许在过渡期继续使用
        if (StringUtils.hasText(tokenType) && !"refresh".equals(tokenType)) {
            throw new RuntimeException("token 类型错误");
        }
        Long userId = Long.valueOf(claims.getSubject());
        int version = claims.get("ver", Integer.class);
        String username = claims.get("uname", String.class);
        if (!StringUtils.hasText(username)) {
            SysUser user = userMapper.selectById(userId);
            username = user == null ? String.valueOf(userId) : user.getUsername();
        }
        String accessToken = tokenService.createAccessToken(userId, version, username);
        String newRefreshToken = tokenService.createRefreshToken(userId, version, username);
        tokenService.removeRefreshToken(refreshToken);
        return TokenResponse.builder().accessToken(accessToken).refreshToken(newRefreshToken).expiresIn(tokenService.getAccessExpireSeconds()).build();
    }

    public void logout(String accessToken, String refreshToken) {
        var claims = tokenService.parse(accessToken);
        redisTemplate.opsForValue().set("auth:blacklist:" + claims.getId(), "1", Duration.ofHours(1));
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

    public List<String> getAccessCodes(String username) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null) {
            return List.of();
        }
        List<Long> roleIds = userRoleMapper.selectList(
            new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, user.getId())
        ).stream().map(SysUserRole::getRoleId).distinct().toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        List<Long> menuIds = roleMenuMapper.selectList(
            new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds)
        ).stream().map(SysRoleMenu::getMenuId).distinct().toList();
        if (menuIds.isEmpty()) {
            return List.of();
        }
        return menuMapper.selectBatchIds(menuIds).stream()
            .filter(menu -> menu.getStatus() != null && menu.getStatus() == 1)
            .map(SysMenu::getAuthCode)
            .filter(StringUtils::hasText)
            .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new))
            .stream()
            .sorted()
            .toList();
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
