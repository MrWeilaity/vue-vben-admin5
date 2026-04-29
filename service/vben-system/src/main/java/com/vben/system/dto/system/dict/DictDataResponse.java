package com.vben.system.dto.system.dict;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 字典项响应。
 */
@Data
@Builder
public class DictDataResponse {
    private Long id;
    private Long typeId;
    private String typeCode;
    private String label;
    private String value;
    private Integer status;
    private Integer sortOrder;
    private String tagType;
    private String tagClass;
    private String cssStyle;
    private Map<String, Object> extJson;
    private String remark;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
