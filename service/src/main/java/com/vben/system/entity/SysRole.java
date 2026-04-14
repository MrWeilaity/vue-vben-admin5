package com.vben.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vben.system.handler.JsonbLongListTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SysRole 组件说明。
 */
@Data
@TableName(value = "sys_role",autoResultMap = true)
public class SysRole {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 名称 */
    private String name;
    /** 状态 */
    private Integer status;
    /** 数据权限范围 */
    private Integer dataScope;
    /** 权限集合JSON */
    @TableField(typeHandler = JsonbLongListTypeHandler.class)
    private List<Long> permissions;
    /** 备注 */
    private String remark;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
