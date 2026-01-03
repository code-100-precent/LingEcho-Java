package com.lingecho.common.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.lingecho.common.core.enums.ResponseCodeEnum;
import lombok.Data;

/**
 * Unified request response class
 *
 * @author heathcetide
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse<T> {

    private int code;

    private String message;

    private T data;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(ResponseCodeEnum.SUCCESS.code());
        response.setMessage(ResponseCodeEnum.SUCCESS.message());
        response.setData(data);
        return response;
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setMessage(message);
        return response;
    }

    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return error(ResponseCodeEnum.SYSTEM_ERROR.code(), message); // 假设 SYSTEM_ERROR.code() 返回的是 int
    }

    public static <T> ApiResponse<T> badRequest(String message) {
        return error(ResponseCodeEnum.NOT_FOUND.code(), message); // 假设 NOT_FOUND.code() 返回的是 int
    }

    // 添加链式调用方法
    public ApiResponse<T> code(int code) { // 修改参数为 int
        this.code = code;
        return this;
    }

    public ApiResponse<T> message(String message) {
        this.message = message;
        return this;
    }
}
