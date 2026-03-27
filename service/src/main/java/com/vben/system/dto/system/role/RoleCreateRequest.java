package com.vben.system.dto.system.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 新增角色请求。
 */
@Data
public class RoleCreateRequest {
    /** 角色名称 */
    @NotBlank
    private String name;
    /** 状态 */
    @NotNull
    private Integer status;
    /** 备注 */
    private String remark;
}
