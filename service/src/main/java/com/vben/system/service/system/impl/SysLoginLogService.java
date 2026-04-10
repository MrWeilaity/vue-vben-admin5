package com.vben.system.service.system.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vben.system.common.PageResult;
import com.vben.system.common.exception.ServiceException;
import com.vben.system.dto.params.LoginLogParams;
import com.vben.system.entity.SysLoginLog;
import com.vben.system.mapper.SysLoginLogMapper;
import com.vben.system.service.system.IpLocationResolver;
import com.vben.system.service.system.ISysLoginLogService;
import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysLoginLogService extends ServiceImpl<SysLoginLogMapper, SysLoginLog> implements ISysLoginLogService {

    private final IpLocationResolver ipLocationResolver;

    @Override
    @Async("operationLogExecutor")
    public void record(String username, String loginIp, String userAgent, boolean success, String operationMsg) {
        try {
            UserAgent parsed = UserAgent.parseUserAgentString(userAgent);
            Browser browser = parsed.getBrowser();
            OperatingSystem operatingSystem = parsed.getOperatingSystem();
            SysLoginLog log = new SysLoginLog();
            log.setUsername(StringUtils.hasText(username) ? username : "-");
            log.setLoginIp(StringUtils.hasText(loginIp) ? loginIp : "-");
            log.setLoginAddress(resolveAddress(loginIp));
            log.setBrowser(browser == null ? "Unknown" : browser.getGroup().getName());
            log.setOs(operatingSystem == null ? "Unknown" : operatingSystem.getGroup().getName());
            log.setStatus(success ? 1 : 0);
            log.setOperationMsg(StringUtils.hasText(operationMsg) ? operationMsg : (success ? "登录成功" : "登录失败"));
            log.setLoginTime(LocalDateTime.now());
            save(log);
        } catch (Exception ex) {
            log.warn("写入登录日志失败: {}", ex.getMessage());
        }
    }

    @Override
    public PageResult<SysLoginLog> getList(LoginLogParams params) {
        LocalDate startTime = params.getStartTime();
        LocalDate endTime = params.getEndTime();
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new ServiceException("开始时间不能大于结束时间");
        }

        Page<SysLoginLog> page = new Page<>(params.getPage(), params.getPageSize());
        var query = lambdaQuery()
                .eq(params.getStatus() != null, SysLoginLog::getStatus, params.getStatus())
                .like(StrUtil.isNotBlank(params.getUsername()), SysLoginLog::getUsername, params.getUsername());
        if (startTime != null) {
            query.ge(SysLoginLog::getLoginTime, startTime.atStartOfDay());
        }
        if (endTime != null) {
            query.lt(SysLoginLog::getLoginTime, endTime.plusDays(1).atStartOfDay());
        }
        Page<SysLoginLog> result = query.orderByDesc(SysLoginLog::getLoginTime).page(page);
        return new PageResult<>(result.getTotal(), result.getRecords());
    }

    private String resolveAddress(String ip) {
        if (!StringUtils.hasText(ip)) {
            return "未知";
        }
        if (isInnerIp(ip)) {
            return "内网IP";
        }
        String value = ipLocationResolver.resolve(ip);
        return StringUtils.hasText(value) ? value : "未知";
    }

    private boolean isInnerIp(String ip) {
        if (ip.startsWith("172.")) {
            String[] segments = ip.split("\\.");
            if (segments.length >= 2) {
                try {
                    int second = Integer.parseInt(segments[1]);
                    if (second >= 16 && second <= 31) {
                        return true;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return "127.0.0.1".equals(ip)
                || "localhost".equalsIgnoreCase(ip)
                || ip.startsWith("192.168.")
                || ip.startsWith("10.")
                || ip.startsWith("169.254.");
    }
}
