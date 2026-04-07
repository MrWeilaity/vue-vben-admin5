package com.vben.system.security;

import com.vben.system.common.exception.UnauthorizedException;
import com.vben.system.entity.SysUser;
import com.vben.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 当前登录用户服务。
 * <p>
 * 负责根据当前请求中的登录态读取用户实体信息，
 * 避免在业务代码中重复编写“先取 userId，再查用户表”的逻辑。
 */
@Service
@RequiredArgsConstructor
public class LoginUserService {
    private final SysUserMapper userMapper;

    /**
     * 获取当前登录用户 ID。
     *
     * @return 当前登录用户 ID
     */
    public Long getCurrentUserId() {
        return SecurityUtils.getUserId();
    }

    /**
     * 获取当前登录用户名。
     *
     * @return 当前登录用户名
     */
    public String getCurrentUsername() {
        return getCurrentUser().getUsername();
    }

    /**
     * 获取当前登录用户实体。
     * <p>
     * 如果登录态存在但数据库中查不到用户，统一按未登录或登录失效处理。
     *
     * @return 当前登录用户实体
     */
    public SysUser getCurrentUser() {
        Long userId = getCurrentUserId();
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new UnauthorizedException("用户不存在或登录已过期");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new UnauthorizedException("账号已被禁用，请重新登录");
        }
        return user;
    }
}
