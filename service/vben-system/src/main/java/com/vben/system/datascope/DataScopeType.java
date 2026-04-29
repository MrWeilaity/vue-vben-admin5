package com.vben.system.datascope;

import lombok.Getter;

import java.util.Arrays;

/**
 * 数据权限范围。
 */
@Getter
public enum DataScopeType {
    /** 全部数据 */
    ALL(1),
    /** 自定义部门数据 */
    CUSTOM(2),
    /** 本部门数据 */
    DEPT(3),
    /** 本部门及以下数据 */
    DEPT_AND_CHILD(4),
    /** 仅本人数据 */
    SELF(5);

    private final int value;

    DataScopeType(int value) {
        this.value = value;
    }

    /**
     * 判断传入值是否属于 1-5 的合法数据权限类型。
     */
    public static boolean isValid(Integer value) {
        if (value == null) {
            return false;
        }
        return Arrays.stream(values()).anyMatch(type -> type.value == value);
    }
}
