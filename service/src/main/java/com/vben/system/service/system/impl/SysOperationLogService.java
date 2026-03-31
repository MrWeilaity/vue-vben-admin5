package com.vben.system.service.system.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vben.system.entity.SysOperationLog;
import com.vben.system.mapper.SysOperationLogMapper;
import com.vben.system.service.system.ISysOperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 操作日志服务。
 */
@Slf4j
@Service
public class SysOperationLogService extends ServiceImpl<SysOperationLogMapper, SysOperationLog>
    implements ISysOperationLogService {

    @Override
    @Async("operationLogExecutor")
    public void saveAsync(SysOperationLog record) {
        if (record == null) {
            return;
        }
        try {
            save(record);
        } catch (Exception e) {
            log.warn("操作日志写入数据库失败: {}", e.getMessage());
        }
    }

    @Override
    public List<SysOperationLog> list(String keyword, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 500));
        LambdaQueryWrapper<SysOperationLog> query = new LambdaQueryWrapper<SysOperationLog>()
            .orderByDesc(SysOperationLog::getId)
            .last("LIMIT " + safeLimit);
        if (StringUtils.hasText(keyword)) {
            query.and(q -> q
                .like(SysOperationLog::getOperatorUsername, keyword)
                .or().like(SysOperationLog::getModule, keyword)
                .or().like(SysOperationLog::getOperationDesc, keyword)
                .or().like(SysOperationLog::getActionType, keyword)
                .or().like(SysOperationLog::getRequestUrl, keyword)
                .or().like(SysOperationLog::getErrorMessage, keyword));
        }
        return list(query);
    }
}
