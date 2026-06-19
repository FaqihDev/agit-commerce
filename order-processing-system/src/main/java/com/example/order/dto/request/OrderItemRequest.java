package com.example.order.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class OrderItemRequest {
    
    @NotNull(message = "productId is mandatory")
    private Long productId;

    @NotNull(message = "quantity is mandatory")
    @Min(value = 1, message = "quantity must be greater than 0")
    private Integer quantity;
}
