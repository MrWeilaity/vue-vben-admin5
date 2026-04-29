package com.vben.admin.controller.system;

import com.vben.common.ApiResponse;
import com.vben.common.PageResult;
import com.vben.system.dto.params.DictDataParams;
import com.vben.system.dto.params.DictTypeParams;
import com.vben.system.dto.system.dict.*;
import com.vben.system.service.system.impl.SysDictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典管理控制器（后台维护接口）。
 */
@Tag(name = "系统管理-字典")
@RestController
@RequestMapping("/api/system/dict")
@RequiredArgsConstructor
@Validated
public class SysDictController {
    private final SysDictService dictService;

    @Operation(summary = "分页查询字典类型")
    @GetMapping("/type/list")
    @PreAuthorize("hasAuthority('System:Dict:List')")
    public ApiResponse<PageResult<DictTypeResponse>> typeList(@Valid DictTypeParams params) {
        return ApiResponse.ok(dictService.typeList(params));
    }

    @Operation(summary = "查询字典类型下拉")
    @GetMapping("/type/options")
    @PreAuthorize("hasAuthority('System:Dict:List')")
    public ApiResponse<List<DictTypeResponse>> typeOptions() {
        return ApiResponse.ok(dictService.typeOptions());
    }

    @Operation(summary = "新增字典类型")
    @PostMapping("/type")
    @PreAuthorize("hasAuthority('System:Dict:Create')")
    public ApiResponse<Void> createType(@Valid @RequestBody DictTypeCreateRequest request) {
        dictService.createType(request);
        return ApiResponse.ok(null);
    }

    @Operation(summary = "更新字典类型")
    @PutMapping("/type/{id}")
    @PreAuthorize("hasAuthority('System:Dict:Edit')")
    public ApiResponse<Void> updateType(@PathVariable Long id, @Valid @RequestBody DictTypeUpdateRequest request) {
        dictService.updateType(id, request);
        return ApiResponse.ok(null);
    }

    @Operation(summary = "修改字典类型编码")
    @PutMapping("/type/{id}/code")
    @PreAuthorize("hasAuthority('System:Dict:Edit')")
    public ApiResponse<Void> updateTypeCode(@PathVariable Long id, @Valid @RequestBody DictTypeCodeUpdateRequest request) {
        dictService.updateTypeCode(id, request.getCode());
        return ApiResponse.ok(null);
    }

    @Operation(summary = "更新字典类型状态")
    @PutMapping("/type/status/{id}")
    @PreAuthorize("hasAuthority('System:Dict:Edit')")
    public ApiResponse<Void> updateTypeStatus(@PathVariable Long id, @Valid @RequestBody DictStatusUpdateRequest request) {
        dictService.updateTypeStatus(id, request.getStatus());
        return ApiResponse.ok(null);
    }

    @Operation(summary = "删除字典类型")
    @DeleteMapping("/type/{id}")
    @PreAuthorize("hasAuthority('System:Dict:Delete')")
    public ApiResponse<Void> deleteType(@PathVariable Long id) {
        dictService.deleteType(id);
        return ApiResponse.ok(null);
    }

    @Operation(summary = "分页查询字典项")
    @GetMapping("/data/list")
    @PreAuthorize("hasAuthority('System:Dict:List')")
    public ApiResponse<PageResult<DictDataResponse>> dataList(@Valid DictDataParams params) {
        return ApiResponse.ok(dictService.dataList(params));
    }

    @Operation(summary = "新增字典项")
    @PostMapping("/data")
    @PreAuthorize("hasAuthority('System:Dict:Create')")
    public ApiResponse<Void> createData(@Valid @RequestBody DictDataCreateRequest request) {
        dictService.createData(request);
        return ApiResponse.ok(null);
    }

    @Operation(summary = "更新字典项")
    @PutMapping("/data/{id}")
    @PreAuthorize("hasAuthority('System:Dict:Edit')")
    public ApiResponse<Void> updateData(@PathVariable Long id, @Valid @RequestBody DictDataUpdateRequest request) {
        dictService.updateData(id, request);
        return ApiResponse.ok(null);
    }

    @Operation(summary = "更新字典项状态")
    @PutMapping("/data/status/{id}")
    @PreAuthorize("hasAuthority('System:Dict:Edit')")
    public ApiResponse<Void> updateDataStatus(@PathVariable Long id, @Valid @RequestBody DictStatusUpdateRequest request) {
        dictService.updateDataStatus(id, request.getStatus());
        return ApiResponse.ok(null);
    }

    @Operation(summary = "删除字典项")
    @DeleteMapping("/data/{id}")
    @PreAuthorize("hasAuthority('System:Dict:Delete')")
    public ApiResponse<Void> deleteData(@PathVariable Long id) {
        dictService.deleteData(id);
        return ApiResponse.ok(null);
    }

    @Operation(summary = "刷新字典缓存")
    @PostMapping("/refreshCache")
    @PreAuthorize("hasAuthority('System:Dict:Refresh')")
    public ApiResponse<Void> refreshCache() {
        dictService.refreshCache();
        return ApiResponse.ok(null);
    }
}
