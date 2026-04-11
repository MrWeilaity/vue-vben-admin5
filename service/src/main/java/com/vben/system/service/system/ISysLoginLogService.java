package com.vben.system.service.system;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vben.system.common.PageResult;
import com.vben.system.dto.params.LoginLogParams;
import com.vben.system.entity.SysLoginLog;

public interface ISysLoginLogService extends IService<SysLoginLog> {
    void record(String username, String loginIp, String userAgent, boolean success, String operationMsg);

    PageResult<SysLoginLog> getList(LoginLogParams params);
}
