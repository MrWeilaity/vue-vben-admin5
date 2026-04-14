package com.vben.system.dto.system.role;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 更新角色请求。
 */
@Data
public class RoleUpdateRequest {
    /** 角色名称 */
    private String name;
    /** 状态 */
    @Min(value = 0, message = "状态值不合法")
    @Max(value = 1, message = "状态值不合法")
    @NotNull
    private Integer status;
    /** 数据权限范围 */
    @Min(value = 1, message = "数据权限范围不合法")
    @Max(value = 5, message = "数据权限范围不合法")
    private Integer dataScope;
    /** 备注 */
    private String remark;
    /** 权限代码 id */
    private List<@Min(value = 1, message = "permissions中的每个值必须大于0") Long> permissions;
    /** 自定义数据权限部门ID集合 */
    private List<@Min(value = 1, message = "dataScopeDeptIds中的每个值必须大于0") Long> dataScopeDeptIds;
}
