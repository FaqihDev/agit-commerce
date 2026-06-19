package com.example.order.controller;

import com.example.order.dto.base.BaseResponse;
import com.example.order.dto.request.CreateOrderRequest;
import com.example.order.dto.response.OrderDetailResponse;
import com.example.order.dto.response.OrderResponse;
import com.example.order.entity.Order;
import com.example.order.enums.ResponseCodeEnum;
import com.example.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BaseResponse<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(request);
        OrderResponse response = new OrderResponse(order.getId(), order.getStatus(), ResponseCodeEnum.SUCCESS_CREATE_ORDER.getMessage());
        return BaseResponse.success(response, ResponseCodeEnum.SUCCESS_CREATE_ORDER);
    }

    @GetMapping("/{id}")
    public BaseResponse<OrderDetailResponse> getOrderById(@PathVariable UUID id) {
        OrderDetailResponse detail = orderService.getOrderWithItems(id);
        return BaseResponse.success(detail, ResponseCodeEnum.SUCCESS_RETRIEVE_DATA);
    }
}
