package com.vben.system.dto.params;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典项分页查询参数。
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DictDataParams extends BasePage {
    /** 字典类型编码。 */
    private String typeCode;
    /** 标签。 */
    private String label;
    /** 字典值。 */
    private String value;
    /** 状态。 */
    private Integer status;
}
