package com.vben.system.dto.params;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
public class PostParams extends BasePage {
    /**
     * 岗位 ID。
     */
    private Long id;
    /**
     * 岗位名称。
     */
    private String name;
    /**
     * 岗位状态。
     */
    private Integer status;
    /**
     * 岗位备注。
     */
    private String remark;
    /**
     * 开始创建时间。
     */
    private LocalDate startTime;
    /**
     * 结束创建时间。
     */
    private LocalDate endTime;
}
