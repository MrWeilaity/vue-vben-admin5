package com.vben.system.controller.system;

import com.vben.system.common.ApiResponse;
import com.vben.system.dto.system.log.OperationLogRecord;
import com.vben.system.service.system.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 操作日志控制器。
 */
@Tag(name = "系统管理-操作日志")
@RestController
@RequestMapping("/api/system/log/operation")
@RequiredArgsConstructor
public class SysOperationLogController {

    private final OperationLogService operationLogService;

    @Operation(summary = "查询操作日志列表")
    @GetMapping("/list")
    public ApiResponse<List<OperationLogRecord>> list(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false, defaultValue = "200") Integer limit
    ) {
        return ApiResponse.ok(operationLogService.list(keyword, limit));
    }
}
