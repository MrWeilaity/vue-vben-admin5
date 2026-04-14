package com.vben.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户-数据权限部门关联实体。
 */
@Data
@TableName("sys_user_dept")
public class SysUserDept {
    /** 用户ID */
    private Long userId;
    /** 部门ID */
    private Long deptId;
}
