package com.vben.system.service.system;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vben.system.entity.SysOperationLog;

import java.util.List;

public interface ISysOperationLogService extends IService<SysOperationLog> {
    void saveAsync(SysOperationLog log);

    List<SysOperationLog> list(String keyword, int limit);
}
