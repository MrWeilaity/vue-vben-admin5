package com.vben.system.service.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vben.system.entity.SysRole;
import com.vben.system.mapper.SysRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色业务服务，处理角色增删改查。
 */
@Service
@RequiredArgsConstructor
public class SysRoleService {

    private final SysRoleMapper roleMapper;

    /**
     * 查询角色列表（按 ID 倒序）。
     *
     * @return 角色列表
     */
    public List<SysRole> list() {
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>().orderByDesc(SysRole::getId));
    }

    /**
     * 新增角色。
     *
     * @param role 角色实体
     */
    public void create(SysRole role) {
        roleMapper.insert(role);
    }

    /**
     * 更新角色。
     *
     * @param id   角色 ID
     * @param role 角色实体
     */
    public void update(Long id, SysRole role) {
        role.setId(id);
        roleMapper.updateById(role);
    }

    /**
     * 删除角色。
     *
     * @param id 角色 ID
     */
    public void delete(Long id) {
        roleMapper.deleteById(id);
    }
}
