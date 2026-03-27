package com.vben.system.dto.system.role;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色返回对象。
 */
@Data
@Builder
public class RoleResponse {
    /** 角色ID */
    private String id;
    /** 角色名称 */
    private String name;
    /** 状态 */
    private Integer status;
    /** 备注 */
    private String remark;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 权限标识集合 */
    private List<String> permissions;
}
