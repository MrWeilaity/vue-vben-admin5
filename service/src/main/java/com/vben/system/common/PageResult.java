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
    private long total;
    private List<T> records;
}
