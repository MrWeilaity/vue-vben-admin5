package com.vben.system.dto.params;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
public class OperationLogParams extends BasePage {
    /**
     * 操作状态：1成功 0失败
     */
    private Integer success;
    /**
     * 系统模块
     */
    private String module;
    /**
     * 开始操作时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startTime;
    /**
     * 结束操作时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endTime;
}
