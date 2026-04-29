package com.vben.system.dto.system.dict;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 修改字典编码请求。
 */
@Data
public class DictTypeCodeUpdateRequest {
    /** 新字典编码。 */
    @NotBlank(message = "字典编码不能为空")
    private String code;
}
