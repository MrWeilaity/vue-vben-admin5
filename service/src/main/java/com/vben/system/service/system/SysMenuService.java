package com.vben.system.service.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vben.system.entity.SysMenu;
import com.vben.system.mapper.SysMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 菜单业务服务，负责菜单树数据的基础增删改查。
 */
@Service
@RequiredArgsConstructor
public class SysMenuService {

    private final SysMenuMapper menuMapper;

    /**
     * 查询菜单列表（按 ID 升序）。
     *
     * @return 菜单列表
     */
    public List<SysMenu> list() {
        return menuMapper.selectList(new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getId));
    }

    /**
     * 新增菜单。
     *
     * @param menu 菜单实体
     */
    public void create(SysMenu menu) {
        menuMapper.insert(menu);
    }

    /**
     * 更新菜单。
     *
     * @param id   菜单 ID
     * @param menu 菜单实体
     */
    public void update(Long id, SysMenu menu) {
        menu.setId(id);
        menuMapper.updateById(menu);
    }

    /**
     * 删除菜单。
     *
     * @param id 菜单 ID
     */
    public void delete(Long id) {
        menuMapper.deleteById(id);
    }
}
