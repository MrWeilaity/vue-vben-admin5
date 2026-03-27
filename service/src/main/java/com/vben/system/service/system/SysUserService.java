package com.vben.system.service.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vben.system.entity.SysUser;
import com.vben.system.mapper.SysUserMapper;
import com.vben.system.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户业务服务，封装用户增删改查与下线控制。
 */
@Service
@RequiredArgsConstructor
public class SysUserService {

    private final SysUserMapper userMapper;
    private final AuthService authService;

    /**
     * 查询用户列表（按 ID 倒序）。
     *
     * @return 用户列表
     */
    public List<SysUser> list() {
        return userMapper.selectList(new LambdaQueryWrapper<SysUser>().orderByDesc(SysUser::getId));
    }

    /**
     * 新增用户。
     *
     * @param user 用户实体
     */
    public void create(SysUser user) {
        userMapper.insert(user);
    }

    /**
     * 更新用户。
     *
     * @param id   用户 ID
     * @param user 用户实体
     */
    public void update(Long id, SysUser user) {
        user.setId(id);
        userMapper.updateById(user);
    }

    /**
     * 删除用户并强制该用户 token 失效。
     *
     * @param id 用户 ID
     */
    public void delete(Long id) {
        userMapper.deleteById(id);
        authService.forceOffline(id);
    }

    /**
     * 强制下线用户。
     *
     * @param id 用户 ID
     */
    public void forceOffline(Long id) {
        authService.forceOffline(id);
    }
}
