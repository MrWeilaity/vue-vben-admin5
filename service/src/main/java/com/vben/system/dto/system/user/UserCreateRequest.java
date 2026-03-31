package com.vben.system.dto.system.user;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/**
 * 新增用户请求。
 */
@Data
public class UserCreateRequest {
    /** 用户名 */
    @NotBlank
    private String username;
    /** 昵称 */
    @NotBlank
    private String nickname;
    /** 密码 */
    @NotBlank
    private String password;
    /** 部门ID */
    @NotNull
    @Min(value = 1, message = "部门ID必须大于0")
    private Long deptId;
    /** 邮箱 */
    @Email(message = "邮箱格式不正确")
    private String email;
    /** 手机号 */
    @Pattern(
            regexp = "^1[3-9]\\d{9}$",
            message = "手机号格式不正确"
    )
    private String mobile;
    /** 状态 */
    @NotNull
    @Min(value = 0, message = "状态值不合法")
    @Max(value = 1, message = "状态值不合法")
    private Integer status;
    /** 数据权限范围 */
    @Min(value = 0, message = "数据权限范围不合法")
    private Integer dataScope;
    /** 备注 */
    @Size(max = 255, message = "备注长度不能超过255")
    private String remark;
    /** 角色ID集合 */
    private List<@Min(value = 1, message = "角色ID必须大于0") Long> roleIds;
    /** 岗位ID集合 */
    private List<@Min(value = 1, message = "岗位ID必须大于0") Long> postIds;
}
