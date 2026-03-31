package com.vben.system.dto.system.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户返回对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    /** 用户ID */
    private Long id;
    /** 用户名 */
    private String username;
    /** 昵称 */
    private String nickname;
    /** 部门ID */
    private Long deptId;
    /** 邮箱 */
    private String email;
    /** 手机号 */
    private String mobile;
    /** 状态 */
    private Integer status;
    /** 数据权限范围 */
    private Integer dataScope;
    /** 备注 */
    private String remark;
    /** 角色ID集合 */
    private List<String> roleIds;
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
