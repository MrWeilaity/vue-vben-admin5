package com.vben.system.dto.system.dict;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 新增字典项请求。
 */
@Data
public class DictDataCreateRequest {
    /** 字典类型编码。 */
    @NotBlank(message = "字典类型编码不能为空")
    private String typeCode;
    /** 标签。 */
    @NotBlank(message = "字典标签不能为空")
    private String label;
    /** 值。 */
    @NotBlank(message = "字典值不能为空")
    private String value;
    /** 状态。 */
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值不合法")
    @Max(value = 1, message = "状态值不合法")
    private Integer status;
    /** 是否默认。 */
    @NotNull(message = "默认值标记不能为空")
    @Min(value = 0, message = "默认值标记不合法")
    @Max(value = 1, message = "默认值标记不合法")
    private Integer isDefault;
    /** 排序。 */
    @NotNull(message = "排序不能为空")
    private Integer sortOrder;
    /** 标签类型。 */
    private String tagType;
    /** 标签 class。 */
    private String tagClass;
    /** 行内样式。 */
    private String cssStyle;
    /** 扩展属性。 */
    private Map<String, Object> extJson;
    /** 备注。 */
    private String remark;
}
