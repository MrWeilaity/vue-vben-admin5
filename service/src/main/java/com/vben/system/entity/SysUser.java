package com.vben.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.vben.system.dto.system.user.UserResponse;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * SysUser 组件说明。
 */
@Data
@TableName("sys_user")
public class SysUser {
    /**
     * 主键ID
     */
    private Long id;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码(密文)
     */
    private String password;
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 部门ID
     */
    private Long deptId;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 手机号
     */
    private String mobile;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 数据权限范围
     */
    private Integer dataScope;
    /**
     * 备注
     */
    private String remark;
    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;
    /**
     * 最后登录IP
     */
    private String lastLoginIp;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    public UserResponse toUserResponse() {
        return UserResponse.builder()
                .id(getId())
                .username(getUsername())
                .nickname(getNickname())
                .deptId(getDeptId())
                .email(getEmail())
                .mobile(getMobile())
                .status(getStatus())
                .dataScope(getDataScope())
                .remark(getRemark())
                .createTime(getCreateTime())
                .build();


    }
}
