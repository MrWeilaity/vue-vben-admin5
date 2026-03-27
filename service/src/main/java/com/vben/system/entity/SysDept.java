package com.vben.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * SysDept 组件说明。
 */
@Data
@TableName("sys_dept")
public class SysDept {
    /** 主键ID */
    private Long id;
    /** 父级ID */
    private Long pid;
    /** 名称 */
    private String name;
    /** 状态 */
    private Integer status;
    /** 备注 */
    private String remark;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
