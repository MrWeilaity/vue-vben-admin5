package com.vben.system.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 重置密码请求参数。
 */
@Data
public class UserPasswordResetRequest {
    /** 新密码 */
    @NotBlank
    private String newPassword;
}
