package com.vben.system.service.system;

import com.vben.common.PageResult;
import com.vben.system.dto.params.BasePage;
import com.vben.system.dto.system.user.OnlineUserResponse;

public interface ISysOnlineUserService {
    PageResult<OnlineUserResponse> list(BasePage params);

    void offline(String sessionId);
}
