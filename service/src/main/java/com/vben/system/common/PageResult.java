package com.vben.system.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * PageResult 组件说明。
 */
@Data
@AllArgsConstructor
public class PageResult<T> {
    /**
     * 数据库总条数
     */
    private long total;
    /**
     * 本次返回的数据
     */
    private List<T> items;
}
