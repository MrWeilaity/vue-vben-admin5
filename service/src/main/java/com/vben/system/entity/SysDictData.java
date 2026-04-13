package com.vben.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.vben.system.handler.JsonbTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 字典项实体。
 */
@Data
@TableName(value = "sys_dict_data", autoResultMap = true)
public class SysDictData {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 类型ID。 */
    private Long typeId;
    /** 字典编码（冗余字段）。 */
    private String typeCode;
    /** 展示标签。 */
    private String label;
    /** 字典值。 */
    private String value;
    /** 状态：1启用，0停用。 */
    private Integer status;
    /** 排序值。 */
    private Integer sortOrder;
    /** 标签类型。 */
    private String tagType;
    /** 标签样式 class。 */
    private String tagClass;
    /** 行内样式。 */
    private String cssStyle;
    /** 扩展 JSON。 */
    @com.baomidou.mybatisplus.annotation.TableField(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> extJson;
    /** 备注。 */
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
