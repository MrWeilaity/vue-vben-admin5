package com.vben.system.service.system;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vben.system.dto.params.DictDataParams;
import com.vben.system.dto.params.DictTypeParams;
import com.vben.system.dto.system.dict.*;
import com.vben.common.PageResult;
import com.vben.system.entity.SysDictType;

import java.util.List;
import java.util.Map;

/**
 * 字典服务接口。
 */
public interface ISysDictService extends IService<SysDictType> {
    PageResult<DictTypeResponse> typeList(DictTypeParams params);

    List<DictTypeResponse> typeOptions();

    void createType(DictTypeCreateRequest request);

    void updateType(Long id, DictTypeUpdateRequest request);

    void updateTypeCode(Long id, String newCode);

    void updateTypeStatus(Long id, Integer status);

    void deleteType(Long id);

    PageResult<DictDataResponse> dataList(DictDataParams params);

    List<DictDataResponse> dataByTypeCode(String typeCode, boolean onlyEnabled);

    Map<String, List<DictDataResponse>> dataByTypeCodes(List<String> typeCodes, boolean onlyEnabled);

    void createData(DictDataCreateRequest request);

    void updateData(Long id, DictDataUpdateRequest request);

    void updateDataStatus(Long id, Integer status);

    void deleteData(Long id);

    void refreshCache();
}
