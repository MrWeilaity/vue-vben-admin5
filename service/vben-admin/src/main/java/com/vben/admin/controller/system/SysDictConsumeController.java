package com.vben.admin.controller.system;

import com.vben.common.ApiResponse;
import com.vben.system.dto.system.dict.DictDataResponse;
import com.vben.system.service.system.impl.SysDictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 字典消费接口（面向业务模块/前端读取）。
 */
@Tag(name = "系统管理-字典消费")
@RestController
@RequestMapping("/api/system/dict/consume")
@RequiredArgsConstructor
@Validated
public class SysDictConsumeController {
    private final SysDictService dictService;

    @Operation(summary = "按字典编码读取字典项")
    @GetMapping("/items")
    public ApiResponse<List<DictDataResponse>> itemsByTypeCode(@RequestParam("typeCode") String typeCode,
                                                               @RequestParam(value = "onlyEnabled", defaultValue = "true") boolean onlyEnabled) {
        return ApiResponse.ok(dictService.dataByTypeCode(typeCode, onlyEnabled));
    }

    @Operation(summary = "批量按字典编码读取字典项")
    @GetMapping("/batch")
    public ApiResponse<Map<String, List<DictDataResponse>>> batchItems(@RequestParam("typeCodes") String typeCodes,
                                                                        @RequestParam(value = "onlyEnabled", defaultValue = "true") boolean onlyEnabled) {
        return ApiResponse.ok(dictService.dataByTypeCodes(Arrays.stream(typeCodes.split(",")).toList(), onlyEnabled));
    }
}
