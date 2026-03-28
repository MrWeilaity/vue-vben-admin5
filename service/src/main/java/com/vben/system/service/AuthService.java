package com.vben.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vben.system.dto.auth.LoginRequest;
import com.vben.system.dto.auth.TokenResponse;
import com.vben.system.entity.SysUser;
import com.vben.system.mapper.SysUserMapper;
import com.vben.system.security.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * AuthService 组件说明。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService tokenService;
    private final StringRedisTemplate redisTemplate;

    public TokenResponse login(LoginRequest request, String ip) {
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
}
