package com.vben.system.dto.params;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户列表的查询条件
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserParams extends BasePage {
    /**
     * 用户名
     */
    private String username;
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 用户状态
     */
    private Integer status;
}
