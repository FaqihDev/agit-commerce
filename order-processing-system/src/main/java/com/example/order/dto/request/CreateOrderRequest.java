package com.example.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

import lombok.Data;

@Data
public class CreateOrderRequest {
    
    @NotBlank(message = "userId is mandatory")
    private String userId;

    @NotEmpty(message = "items cannot be empty")
    private List<OrderItemRequest> items;
}
