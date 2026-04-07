package com.vben.system.dto.system.post;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 新增岗位请求。
 */
@Data
public class PostCreateRequest {
    /**
     * 岗位名称。
     */
    @NotBlank(message = "岗位名称不能为空")
    private String name;
    /**
     * 岗位状态。
     */
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值不合法")
    @Max(value = 1, message = "状态值不合法")
    private Integer status;
    /**
     * 备注。
     */
    private String remark;
}
