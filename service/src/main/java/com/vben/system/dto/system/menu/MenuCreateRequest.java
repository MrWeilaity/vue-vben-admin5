package com.vben.system.dto.system.menu;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 新增菜单请求。
 */
@Data
public class MenuCreateRequest {
    /**
     * 父菜单ID
     */
    private Long pid;
    /**
     * 菜单名称
     */
    @NotBlank
    private String name;
    /**
     * 路由路径
     */
    @NotBlank
    private String path;
    /**
     * 菜单类型
     */
    @NotBlank
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
    @NotNull
    private Integer status;
    /**
     * 菜单元数据
     */
    private Map<String, Object> meta;
}
