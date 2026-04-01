package com.vben.system.controller.system;

import com.vben.system.common.ApiResponse;
import com.vben.system.common.PageResult;
import com.vben.system.dto.params.OperationLogParams;
import com.vben.system.entity.SysOperationLog;
import com.vben.system.service.system.ISysOperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 操作日志控制器。
 */
@Tag(name = "系统管理-操作日志")
@RestController
@RequestMapping("/api/system/log/operation")
@RequiredArgsConstructor
@Validated
public class SysOperationLogController {

    private final ISysOperationLogService operationLogService;

    @Operation(summary = "查询操作日志列表")
    @GetMapping("/list")
    public ApiResponse<PageResult<SysOperationLog>> list(@Valid OperationLogParams operationLogParams) {
        return ApiResponse.ok(operationLogService.getList(operationLogParams));
    }
}
