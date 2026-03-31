package com.vben.system.dto.core;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "基础用户信息")
public class BasicUserInfo {
    /**
     * 用户头像
     */
    @Schema(description = "头像")
    private String avatar;
    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称")
    private String realName;
    /**
     * 用户角色
     */
    @Schema(description = "用户角色")
    private List<String> roles;
    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private String userId;
    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String username;
}