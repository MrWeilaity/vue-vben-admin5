package com.vben.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.vben.system.handler.JsonbTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 系统操作日志实体。
 */
@Data
@TableName(value = "sys_operation_log", autoResultMap = true)
public class SysOperationLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime occurTime;
    private Long operatorUserId;
    private String operatorUsername;
    private String operatorDept;
    private String module;
    private String operationDesc;
    private String actionType;
    private String clientIp;
    private String clientAddress;
    private String requestMethod;
    private String requestUrl;
    private String requestParams;
    private Integer bizStatusCode;
    private Integer httpStatusCode;
    private Long durationMs;
    private Integer success;
    private String errorMessage;
    @TableField(value = "ext_data", typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> extData;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
