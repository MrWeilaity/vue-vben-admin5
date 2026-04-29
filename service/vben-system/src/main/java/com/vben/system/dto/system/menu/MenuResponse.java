package com.vben.system.dto.system.menu;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 菜单返回对象。
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class MenuResponse {
    /**
     * 菜单ID
     */
    private Long id;
    /**
     * 父菜单ID
     */
    private Long pid;
    /**
     * 菜单名称
     */
    private String name;
    /**
     * 路由路径
     */
    private String path;
    /**
     * 菜单类型
     * 'catalog',
     * 'menu',
     * 'embedded',
     * 'link',
     * 'button',
     */
    private String type;
    /**
     * 组件路径
     */
    private String component;
    /**
     * 权限标识
     */
    private String authCode;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 菜单元数据
     */
    private Map<String, Object> meta;
    private List<MenuResponse> children;
}
