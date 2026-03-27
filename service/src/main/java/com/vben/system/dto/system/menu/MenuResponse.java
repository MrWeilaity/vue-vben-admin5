package com.vben.system.dto.system.menu;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 菜单返回对象。
 */
@Data
@Builder
public class MenuResponse {
    /** 菜单ID */
    private String id;
    /** 父菜单ID */
    private String pid;
    /** 菜单名称 */
    private String name;
    /** 路由路径 */
    private String path;
    /** 菜单类型 */
    private String type;
    /** 组件路径 */
    private String component;
    /** 权限标识 */
    private String authCode;
    /** 状态 */
    private Integer status;
    /** 菜单元数据 */
    private Map<String, Object> meta;
    /** 创建时间 */
    private LocalDateTime createTime;
}
