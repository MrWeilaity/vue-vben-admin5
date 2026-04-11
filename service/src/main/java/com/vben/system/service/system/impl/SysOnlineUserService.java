package com.vben.system.service.system.impl;

import com.vben.system.common.exception.BadRequestException;
import com.vben.system.dto.system.user.OnlineUserResponse;
import com.vben.system.entity.SysDept;
import com.vben.system.entity.SysUser;
import com.vben.system.security.LoginUserService;
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
    public List<OnlineUserResponse> list() {
        List<AuthSessionService.SessionRecord> sessions = authSessionService.listAllSessions();
        if (sessions.isEmpty()) {
            return List.of();
        }
        Set<Long> userIds = sessions.stream().map(AuthSessionService.SessionRecord::getUserId).collect(Collectors.toSet());
        Map<Long, SysUser> userMap = sysUserService.listByIds(userIds).stream().collect(Collectors.toMap(SysUser::getId, v -> v));
        Set<Long> deptIds = userMap.values().stream().map(SysUser::getDeptId).filter(id -> id != null && id > 0).collect(Collectors.toSet());
        Map<Long, String> deptMap = sysDeptService.listByIds(deptIds).stream().collect(Collectors.toMap(SysDept::getId, SysDept::getName));
        String currentSessionId = loginUserService.getCurrentSessionId();

        return sessions.stream().map(item -> {
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
