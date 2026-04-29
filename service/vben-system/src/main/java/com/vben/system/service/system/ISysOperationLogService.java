package com.vben.system.service.system;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vben.common.PageResult;
import com.vben.system.dto.params.OperationLogParams;
import com.vben.system.entity.SysOperationLog;


public interface ISysOperationLogService extends IService<SysOperationLog> {
    void saveAsync(SysOperationLog log);

    PageResult<SysOperationLog> getList(OperationLogParams operationLogParams);
}
