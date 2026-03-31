package com.vben.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 岗位实体。
 */
@Data
@TableName("sys_post")
public class SysPost {
    private Long id;
    private String name;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
