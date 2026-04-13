package com.vben.system.dto.system.dict;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 新增字典类型请求。
 */
@Data
public class DictTypeCreateRequest {
    /** 名称。 */
    @NotBlank(message = "字典名称不能为空")
    private String name;
    /** 编码。 */
    @NotBlank(message = "字典编码不能为空")
    private String code;
    /** 状态。 */
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值不合法")
    @Max(value = 1, message = "状态值不合法")
    private Integer status;
    /** 备注。 */
    private String remark;
}
