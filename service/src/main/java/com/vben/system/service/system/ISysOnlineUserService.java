package com.vben.system.service.system;

import com.vben.system.dto.system.user.OnlineUserResponse;

import java.util.List;

public interface ISysOnlineUserService {
    List<OnlineUserResponse> list();

    void offline(String sessionId);
}
