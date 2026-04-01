package com.vben.system.dto.params;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
public class RoleParams extends BasePage {
    /**
     * 角色名字
     */
    private String name;
    /**
     *
     * 角色状态
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;
    /**
     * 开始创建时间
     */
    private LocalDate startTime;
    /**
     * 结束创建时间
     */
    private LocalDate endTime;
}
