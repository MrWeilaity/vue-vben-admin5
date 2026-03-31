package com.vben.system.dto.params;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class BasePage {
    /**
     * 页数
     */
    @Min(value = 1, message = "页码必须大于等于1")
    private int page;
    /**
     * 没页大小
     */
    @Min(value = 1, message = "每页数量必须大于等于1")
    @Max(value = 100, message = "每页数量不能超过100")
    private int pageSize;
}
