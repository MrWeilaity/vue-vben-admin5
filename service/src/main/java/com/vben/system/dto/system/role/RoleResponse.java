package com.vben.system.dto.system.role;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色返回对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {
    /**
     * 角色ID
     */
    private Long id;
    /**
     * 角色名称
     */
    private String name;
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
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /**
     * 权限标识集合
     */
    private List<Long> permissions;
    /**
     * 自定义数据权限部门ID集合
     */
    private List<Long> dataScopeDeptIds;
}
