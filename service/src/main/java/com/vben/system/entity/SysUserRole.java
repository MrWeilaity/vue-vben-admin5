package com.vben.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * SysUserRole 组件说明。
 */
@Data
@TableName("sys_user_role")
public class SysUserRole {
    /** 用户ID */
    private Long userId;
    /** 角色ID */
    private Long roleId;
}
