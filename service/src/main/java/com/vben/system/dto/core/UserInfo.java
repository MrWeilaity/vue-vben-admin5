package com.vben.system.dto.core;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "/user/info 接口返回的用户信息")
public class UserInfo extends BasicUserInfo {
    /**
     * 用户描述
     */
    @Schema(description = "用户描述")
    private String desc;
    /**
     * 首页地址
     */
    @Schema(description = "首页地址")
    private String homePath;
    /**
     * accessToken
     */
    @Schema(description = "accessToken")
    private String token;
}
