package com.vben.system.dto.system.role;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 新增角色请求。
 */
@Data
public class RoleCreateRequest {
    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    private String name;
    /**
     * 状态
     */
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值不合法")
    @Max(value = 1, message = "状态值不合法")
    private Integer status;
    /**
     * 备注
     */
    private String remark;
    /**
     * 权限代码 id
     */
    private List<@Min(value = 1, message = "permissions中的每个值必须大于0") Long> permissions;
}
