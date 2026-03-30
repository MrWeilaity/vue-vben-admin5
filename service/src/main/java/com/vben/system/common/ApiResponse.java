package com.vben.system.common;

import lombok.Data;

/**
 * ApiResponse 组件说明。
 */
@Data
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> resp = new ApiResponse<>();
        resp.setCode(0);
        resp.setMessage("ok");
        resp.setData(data);
        return resp;
    }

    public static <T> ApiResponse<T> fail(int code, String message) {
        ApiResponse<T> resp = new ApiResponse<>();
        resp.setCode(code);
        resp.setMessage(message);
        resp.setData(null);
        return resp;
    }
}
