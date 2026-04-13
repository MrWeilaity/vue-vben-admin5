package com.vben.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典类型实体。
 */
@Data
@TableName("sys_dict_type")
public class SysDictType {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 字典名称。 */
    private String name;
    /** 字典编码。 */
    private String code;
    /** 状态：1启用，0停用。 */
    private Integer status;
    /** 是否系统内置：1是，0否。 */
    private Integer builtIn;
    /** 备注。 */
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
