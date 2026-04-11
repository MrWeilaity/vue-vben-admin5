package com.vben.system.dto.params;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典类型分页查询参数。
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DictTypeParams extends BasePage {
    /** 字典名称。 */
    private String name;
    /** 字典编码。 */
    private String code;
    /** 状态。 */
    private Integer status;
}
