package com.vben.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 角色-数据权限部门关联实体。
 */
@Data
@TableName("sys_role_dept")
public class SysRoleDept {
    /** 角色ID */
    private Long roleId;
    /** 部门ID */
    private Long deptId;
}
