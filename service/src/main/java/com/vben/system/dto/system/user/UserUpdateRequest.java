package com.vben.system.dto.system.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 更新用户请求。
 */
@Data
public class UserUpdateRequest {
    /** 昵称 */
    @NotBlank
    private String nickname;
    /** 部门ID */
    @NotNull
    private Long deptId;
    /** 邮箱 */
    private String email;
    /** 手机号 */
    private String mobile;
    /** 状态 */
    @NotNull
    private Integer status;
    /** 数据权限范围 */
    private Integer dataScope;
    /** 备注 */
    private String remark;
    /** 角色ID集合 */
    private List<Long> roleIds;
}
