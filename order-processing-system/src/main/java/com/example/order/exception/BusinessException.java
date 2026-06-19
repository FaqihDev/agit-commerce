package com.example.order.exception;

import com.example.order.enums.ResponseCodeEnum;

public class BusinessException extends RuntimeException {
    private final ResponseCodeEnum responseCodeEnum;

    public BusinessException(ResponseCodeEnum responseCodeEnum) {
        super(responseCodeEnum.getMessage());
        this.responseCodeEnum = responseCodeEnum;
    }

    public ResponseCodeEnum getResponseCodeEnum() {
        return responseCodeEnum;
    }
    
    public int getCode() {
        return responseCodeEnum.getCode();
    }
}
