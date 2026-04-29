package com.vben.system.dto.system.dict;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典类型响应。
 */
@Data
@Builder
public class DictTypeResponse {
    private Long id;
    private String name;
    private String code;
    private Integer status;
    private Integer builtIn;
    private String remark;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
