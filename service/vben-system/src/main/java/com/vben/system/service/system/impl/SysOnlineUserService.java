package com.vben.system.service.system.impl;

import com.vben.common.PageResult;
import com.vben.common.exception.BadRequestException;
import com.vben.system.dto.params.BasePage;
import com.vben.system.dto.system.user.OnlineUserResponse;
import com.vben.system.entity.SysDept;
import com.vben.system.entity.SysUser;
import com.vben.system.auth.LoginUserService;
import com.vben.system.service.AuthSessionService;
import com.vben.system.service.system.ISysOnlineUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysOnlineUserService implements ISysOnlineUserService {

    private final AuthSessionService authSessionService;
    private final SysUserService sysUserService;
    private final SysDeptService sysDeptService;
    private final LoginUserService loginUserService;

    @Override
    public PageResult<OnlineUserResponse> list(BasePage params) {
        List<AuthSessionService.SessionRecord> sessions = authSessionService.listAllSessions();
        if (sessions.isEmpty()) {
            return new PageResult<>(0, List.of());
        }
        int total = sessions.size();
        int fromIndex = Math.min((params.getPage() - 1) * params.getPageSize(), total);
        int toIndex = Math.min(fromIndex + params.getPageSize(), total);
        List<AuthSessionService.SessionRecord> pageSessions = sessions.subList(fromIndex, toIndex);
        Set<Long> userIds = pageSessions.stream().map(AuthSessionService.SessionRecord::getUserId).collect(Collectors.toSet());
        Map<Long, SysUser> userMap = userIds.isEmpty()
                ? Map.of()
                : sysUserService.listByIds(userIds).stream().collect(Collectors.toMap(SysUser::getId, v -> v));
        Set<Long> deptIds = userMap.values().stream().map(SysUser::getDeptId).filter(id -> id != null && id > 0).collect(Collectors.toSet());
        Map<Long, String> deptMap = deptIds.isEmpty()
                ? Map.of()
                : sysDeptService.listByIds(deptIds).stream().collect(Collectors.toMap(SysDept::getId, SysDept::getName));
        String currentSessionId = loginUserService.getCurrentSessionId();

        List<OnlineUserResponse> items = pageSessions.stream().map(item -> {
            SysUser user = userMap.get(item.getUserId());
            String deptName = user == null ? "-" : deptMap.getOrDefault(user.getDeptId(), "-");
            return OnlineUserResponse.builder()
                    .sessionId(item.getSessionId())
                    .userId(item.getUserId())
                    .username(item.getUsername())
                    .deptName(deptName)
                    .loginIp(item.getLoginIp())
                    .loginAddress(item.getLoginAddress())
                    .browser(item.getBrowser())
                    .os(item.getOs())
                    .deviceType(item.getDeviceType())
                    .loginTime(item.getLoginTime())
                    .lastAccessTime(item.getLastAccessTime())
                    .expiresAt(item.getRefreshExpireAt())
                    .current(StringUtils.hasText(currentSessionId) && currentSessionId.equals(item.getSessionId()))
                    .build();
        }).toList();
        return new PageResult<>(total, items);
    }

    @Override
    public void offline(String sessionId) {
        String currentSessionId = loginUserService.getCurrentSessionId();
        if (StringUtils.hasText(currentSessionId) && currentSessionId.equals(sessionId)) {
            throw new BadRequestException("不能下线当前会话");
        }
        authSessionService.deleteSession(sessionId);
    }
}
