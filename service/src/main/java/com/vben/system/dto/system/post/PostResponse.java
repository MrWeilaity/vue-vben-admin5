package com.vben.system.dto.system.post;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 岗位返回对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    /**
     * 岗位 ID。
     */
    private Long id;
    /**
     * 岗位名称。
     */
    private String name;
    /**
     * 岗位状态。
     */
    private Integer status;
    /**
     * 岗位备注。
     */
    private String remark;
    /**
     * 创建时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
