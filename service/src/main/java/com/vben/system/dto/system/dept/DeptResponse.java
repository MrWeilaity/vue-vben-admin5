package com.vben.system.dto.system.dept;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 部门返回对象。
 */
@Data
@Builder
public class DeptResponse {
    /** 部门ID */
    private String id;
    /** 父部门ID */
    private String pid;
    /** 部门名称 */
    private String name;
    /** 状态 */
    private Integer status;
    /** 备注 */
    private String remark;
    /** 创建时间 */
    private LocalDateTime createTime;
}
