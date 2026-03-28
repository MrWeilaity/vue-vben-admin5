package com.vben.system.dto.system.log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 操作日志记录。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationLogRecord {
    /** 操作用户 */
    private String username;
    /** 请求IP */
    private String ip;
    /** 请求方法 */
    private String method;
    /** 请求路径 */
    private String path;
    /** 调用目标 */
    private String operation;
    /** 执行结果 */
    private String result;
    /** 错误信息 */
    private String error;
    /** 耗时（毫秒） */
    private Long durationMs;
    /** 记录时间 */
    private LocalDateTime time;
}
