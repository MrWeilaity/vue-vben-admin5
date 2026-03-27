package com.vben.system.dto.system.dept;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新部门请求。
 */
@Data
public class DeptUpdateRequest {
    /** 父部门ID */
    private Long pid;
    /** 部门名称 */
    @NotBlank
    private String name;
    /** 状态 */
    @NotNull
    private Integer status;
    /** 备注 */
    private String remark;
}
