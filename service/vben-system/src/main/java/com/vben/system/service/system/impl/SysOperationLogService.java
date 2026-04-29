package com.vben.system.service.system.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vben.common.PageResult;
import com.vben.common.exception.ServiceException;
import com.vben.system.dto.params.OperationLogParams;
import com.vben.system.entity.SysOperationLog;
import com.vben.system.mapper.SysOperationLogMapper;
import com.vben.system.service.system.IpLocationResolver;
import com.vben.system.service.system.ISysOperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

/**
 * 操作日志服务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysOperationLogService extends ServiceImpl<SysOperationLogMapper, SysOperationLog> implements ISysOperationLogService {

    private final IpLocationResolver ipLocationResolver;

    @Override
    @Async("operationLogExecutor")
    public void saveAsync(SysOperationLog record) {
        if (record == null) {
            return;
        }
        try {
            if (!StringUtils.hasText(record.getClientAddress())) {
                record.setClientAddress(record.getClientIp() == null ? null : ipLocationResolver.resolve(record.getClientIp()));
            }
            save(record);
        } catch (Exception e) {
            log.warn("操作日志写入数据库失败: {}", e.getMessage());
        }
    }

    @Override
    public PageResult<SysOperationLog> getList(OperationLogParams operationLogParams) {
        LocalDate startTime = operationLogParams.getStartTime();
        LocalDate endTime = operationLogParams.getEndTime();
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new ServiceException("开始时间不能大于结束时间");
        }

        Page<SysOperationLog> page = new Page<>(operationLogParams.getPage(), operationLogParams.getPageSize());
        var query = lambdaQuery()
                .eq(operationLogParams.getSuccess() != null, SysOperationLog::getSuccess, operationLogParams.getSuccess())
                .like(StrUtil.isNotBlank(operationLogParams.getModule()), SysOperationLog::getModule, operationLogParams.getModule());
        if (startTime != null) {
            query.ge(SysOperationLog::getOccurTime, startTime.atStartOfDay());
        }
        if (endTime != null) {
            query.lt(SysOperationLog::getOccurTime, endTime.plusDays(1).atStartOfDay());
        }
        Page<SysOperationLog> result = query
                .orderByDesc(SysOperationLog::getOccurTime)
                .page(page);
        return new PageResult<>(result.getTotal(), result.getRecords());

    }
}
