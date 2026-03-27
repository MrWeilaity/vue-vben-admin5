package com.vben.system.dto.system.user;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户返回对象。
 */
@Data
@Builder
public class UserResponse {
    /** 用户ID */
    private String id;
    /** 用户名 */
    private String username;
    /** 昵称 */
    private String nickname;
    /** 部门ID */
    private String deptId;
    /** 邮箱 */
    private String email;
    /** 手机号 */
    private String mobile;
    /** 状态 */
    private Integer status;
    /** 数据权限范围 */
    private Integer dataScope;
    /** 备注 */
    private String remark;
    /** 角色ID集合 */
    private List<String> roleIds;
    /** 创建时间 */
    private LocalDateTime createTime;
}
