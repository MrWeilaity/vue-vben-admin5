package com.vben.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * SysMenu 组件说明。
 */
@Data
@TableName("sys_menu")
public class SysMenu {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 父级ID */
    private Long pid;
    /** 名称 */
    private String name;
    /** 路由路径 */
    private String path;
    /** 菜单类型 */
    private String type;
    /** 组件路径 */
    private String component;
    /** 权限标识 */
    private String authCode;
    /** 菜单元数据JSON */
    private String metaJson;
    /** 状态 */
    private Integer status;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
