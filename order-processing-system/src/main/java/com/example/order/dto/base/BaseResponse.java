package com.example.order.dto.base;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse<T> {
    private int code;
    private String status;
    private String message;
    private T data;

    public static <T> BaseResponse<T> success(T data, String message) {
        return new BaseResponse<>(200, "SUCCESS", message, data);
    }

    public static <T> BaseResponse<T> success(T data, com.example.order.enums.ResponseCodeEnum responseCode) {
        return new BaseResponse<>(responseCode.getCode(), "SUCCESS", responseCode.getMessage(), data);
    }

    public static <T> BaseResponse<T> error(int code, String message) {
        return new BaseResponse<>(code, "ERROR", message, null);
    }
}
