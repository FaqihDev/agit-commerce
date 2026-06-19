package com.example.order.enums;

import org.springframework.http.HttpStatus;

public enum ResponseCodeEnum {
    SUCCESS(HttpStatus.OK, 200, "Success"),
    SUCCESS_RETRIEVE_DATA(HttpStatus.OK, 200, "Successfully retrieved data"),
    SUCCESS_CREATE_ORDER(HttpStatus.CREATED, 201, "Successfully created order"),
    INSUFFICIENT_STOCK(HttpStatus.OK, 204, "Stok tidak tersedia, silahkan hubungi Admin"),
    INVALID_QUANTITY(HttpStatus.BAD_REQUEST, 400, "Quantity must be greater than 0"),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "Order not found"),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "Product not found");

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;

    ResponseCodeEnum(HttpStatus httpStatus, int code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
