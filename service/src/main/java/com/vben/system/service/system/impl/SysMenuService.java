package com.vben.system.service.system.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vben.system.common.exception.ServiceException;
import com.vben.system.dto.system.menu.MenuCreateRequest;
import com.vben.system.dto.system.menu.MenuResponse;
import com.vben.system.dto.system.menu.MenuUpdateRequest;
import com.vben.system.entity.SysMenu;
import com.vben.system.entity.SysRoleMenu;
import com.vben.system.mapper.SysMenuMapper;
import com.vben.system.mapper.SysRoleMenuMapper;
import com.vben.system.service.system.ISysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 菜单业务服务，负责菜单树数据的基础增删改查。
 */
@Service
@RequiredArgsConstructor
public class SysMenuService extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

    private final SysMenuMapper menuMapper;
    private final ObjectMapper objectMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    /**
     * 查询菜单列表（按 ID 升序）。
     *
     * @return 菜单列表
     */
    public List<SysMenu> list() {
        return menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .orderByAsc(SysMenu::getId)
        );
    }

    /**
     * 构建菜单树
     *
     */
    public List<MenuResponse> buildMenuTree() {
        List<SysMenu> menus = list();

        // 按父节点 ID 分组，key = pid，value = 子菜单列表
        Map<Long, List<SysMenu>> menuMap = new LinkedHashMap<>();
        for (SysMenu menu : menus) {
            menuMap.computeIfAbsent(menu.getPid(), k -> new ArrayList<>())
                    .add(menu);
        }

        // 根节点 pid = null
        return buildChildren(menuMap, null);
    }

    /**
     * 递归构建菜单树的子节点列表。
     *
     * <p>根据父节点 ID（pid），从 menuMap 中查找对应的子菜单，
     * 并递归构建其 children，最终生成完整的树结构。</p>
     *
     * <p>实现特点：</p>
     * <ul>
     *     <li>使用 Map 预分组（pid -> 子节点列表），避免重复扫描</li>
     *     <li>递归构建 children，实现无限层级菜单</li>
     *     <li>菜单顺序沿用 list() 查询结果中的 ID 升序</li>
     *     <li>当 children 为空时返回 null（配合 JsonInclude.NON_NULL 不输出）</li>
     * </ul>
     *
     * @param menuMap 菜单分组 Map（key = pid，value = 子菜单列表）
     * @param pid     当前父节点 ID；根节点时为 null
     * @return 当前节点下的所有子菜单
     */
    private List<MenuResponse> buildChildren(Map<Long, List<SysMenu>> menuMap, Long pid) {
        List<SysMenu> childrenMenus = menuMap.get(pid);
        if (childrenMenus == null || childrenMenus.isEmpty()) {
            return Collections.emptyList();
        }

        List<MenuResponse> result = new ArrayList<>(childrenMenus.size());
        for (SysMenu menu : childrenMenus) {
            List<MenuResponse> children = buildChildren(menuMap, menu.getId());

            result.add(MenuResponse.builder()
                    .id(menu.getId())
                    .pid(menu.getPid())
                    .name(menu.getName())
                    .path(menu.getPath())
                    .type(normalizeMenuType(menu.getType()))
                    .component(menu.getComponent())
                    .authCode(menu.getAuthCode())
                    .status(menu.getStatus())
                    .meta(parseMeta(menu.getMetaJson()))
                    .children(children.isEmpty() ? null : children)
                    .build());
        }

        return result;
    }

    /**
     * 标准化菜单类型
     *
     * <p>将菜单类型统一转换为小写（避免前后端不一致问题）</p>
     *
     * <p>示例：</p>
     * <ul>
     *     <li>"MENU" → "menu"</li>
     *     <li>"Button" → "button"</li>
     * </ul>
     *
     * @param type 原始菜单类型
     * @return 转换后的类型（小写），若为空则返回 null
     */
    private String normalizeMenuType(String type) {
        return type == null ? null : type.toLowerCase(Locale.ROOT);
    }

    /**
     * 解析菜单 meta JSON 字符串为 Map。
     *
     * <p>处理规则：</p>
     * <ul>
     *     <li>null 或空字符串：返回空 Map</li>
     *     <li>解析失败：返回空 Map，避免接口报错</li>
     * </ul>
     *
     * @param metaJson JSON 字符串
     * @return 解析后的 Map，永不为 null
     */
    private Map<String, Object> parseMeta(String metaJson) {
        if (metaJson == null || metaJson.isBlank()) {
            return Collections.emptyMap();
        }

        try {
            return objectMapper.readValue(metaJson, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }


    /**
     * 新增菜单。
     *
     * @param createRequest 新增菜单实体
     */
    public void create(MenuCreateRequest createRequest) {
        checkPidLegitimate(null, createRequest.getPid());
        SysMenu createMenu = new SysMenu();
        createMenu.setPid(createRequest.getPid());
        createMenu.setName(createRequest.getName());
        createMenu.setPath(createRequest.getPath());
        createMenu.setType(normalizeMenuType(createRequest.getType()));
        createMenu.setComponent(createRequest.getComponent());
        createMenu.setAuthCode(createRequest.getAuthCode());
        createMenu.setStatus(createRequest.getStatus());
        createMenu.setMetaJson(writeMeta(createRequest.getMeta()));
        menuMapper.insert(createMenu);
    }

    private String writeMeta(Map<String, Object> meta) {
        if (meta == null || meta.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(meta);
        } catch (Exception ex) {
            throw new ServiceException("菜单 meta 字段不是合法 JSON");
        }
    }

    /**
     * 更新菜单。
     *
     * @param id            菜单 ID
     * @param updateRequest 菜单实体
     */
    public void update(Long id, MenuUpdateRequest updateRequest) {
        SysMenu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new ServiceException("菜单不存在");
        }
        checkPidLegitimate(id, updateRequest.getPid());
        menu.setPid(updateRequest.getPid());
        menu.setName(updateRequest.getName());
        menu.setPath(updateRequest.getPath());
        menu.setType(normalizeMenuType(updateRequest.getType()));
        menu.setComponent(updateRequest.getComponent());
        menu.setAuthCode(updateRequest.getAuthCode());
        menu.setStatus(updateRequest.getStatus());
        menu.setMetaJson(writeMeta(updateRequest.getMeta()));
        menuMapper.updateById(menu);
    }

    /**
     * 检查创建和更新的菜单时候合法
     * 上级菜单不能是按钮 上级菜单不能是自己
     *
     * @param pid 上级菜单ID
     * @param id  增加或者更新的菜单ID
     */
    private void checkPidLegitimate(Long id, Long pid) {
        // 根节点允许 pid 为 null
        if (pid == null) {
            return;
        }

        if (pid <= 0) {
            throw new ServiceException("请选择合适的菜单作为父菜单");
        }

        if (Objects.equals(id, pid)) {
            throw new ServiceException("上级菜单不能设置为自己");
        }

        SysMenu parentMenu = menuMapper.selectById(pid);
        if (parentMenu == null) {
            throw new ServiceException("上级菜单不存在");
        }

        if ("button".equalsIgnoreCase(parentMenu.getType())) {
            throw new ServiceException("上级菜单不能是按钮");
        }

        // 编辑时：禁止把父菜单设置为自己的子孙节点，避免形成环
        if (id != null && isDescendant(pid, id)) {
            throw new ServiceException("上级菜单不能设置为自己的子菜单");
        }
    }

    /**
     * 检查菜单的父菜单是否构成循环依赖
     *
     * @param pid       父菜单ID
     * @param currentId 当前菜单
     * @return true 构成循环 false 不构成循环
     */
    private boolean isDescendant(Long pid, Long currentId) {
        Long parentId = pid;
        while (parentId != null) {
            if (Objects.equals(parentId, currentId)) {
                return true;
            }
            SysMenu parent = menuMapper.selectById(parentId);
            if (parent == null) {
                break;
            }
            parentId = parent.getPid();
        }
        return false;
    }

    /**
     * 删除菜单。
     *
     * @param id 菜单 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        SysMenu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new ServiceException("菜单不存在或已被删除");
        }
        Long childCount = menuMapper.selectCount(
                new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getPid, id)
        );
        if (childCount != null && childCount > 0) {
            throw new ServiceException("该菜单下仍有 " + childCount + " 个子菜单，请先删除子菜单后再删除");
        }

        Long roleCount = roleMenuMapper.selectCount(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getMenuId, id)
        );
        if (roleCount != null && roleCount > 0) {
            throw new ServiceException("该菜单已分配给 " + roleCount + " 个角色，请先解除角色授权后再删除");
        }

        menuMapper.deleteById(id);
    }

    /**
     * 校验菜单名称是否已存在。
     *
     * @param name      菜单名称
     * @param excludeId 排除的菜单 ID
     * @return 是否存在
     */
    public boolean existsByName(String name, Long excludeId) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getName, name);
        if (excludeId != null) {
            wrapper.ne(SysMenu::getId, excludeId);
        }
        return menuMapper.selectCount(wrapper) > 0;
    }

    /**
     * 校验菜单路径是否已存在。
     *
     * @param path      菜单路径
     * @param excludeId 排除的菜单 ID
     * @return 是否存在
     */
    public boolean existsByPath(String path, Long excludeId) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getPath, path);
        if (excludeId != null) {
            wrapper.ne(SysMenu::getId, excludeId);
        }
        return menuMapper.selectCount(wrapper) > 0;
    }
}
