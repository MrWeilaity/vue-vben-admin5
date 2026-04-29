package com.vben.system.dto.system.dept;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门返回对象。
 */
@Data
@Builder
public class DeptResponse {
    /**
     * 部门ID
     */
    private Long id;
    /**
     * 父部门ID
     */
    private Long pid;
    /**
     * 部门名称
     */
    private String name;
    /**
     * 状态
     */
    private Integer status;
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
     * 子部门
     */
    private List<DeptResponse> children;
}
