package com.example.order.controller;

import com.example.order.dto.request.CreateOrderRequest;
import com.example.order.dto.request.OrderItemRequest;
import com.example.order.dto.response.OrderDetailResponse;
import com.example.order.dto.response.OrderResponse;
import com.example.order.entity.Order;
import com.example.order.enums.OrderStatusEnum;
import com.example.order.service.OrderService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createOrder_Success() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId("user123");
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(2);
        request.setItems(List.of(item));

        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setStatus(OrderStatusEnum.PENDING.name());

        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(order);

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("201"))
                .andExpect(jsonPath("$.data.orderId").value(order.getId().toString()))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void getOrderById_Success() throws Exception {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);
        order.setStatus("PAID");
        order.setTotalAmount(new BigDecimal("100.00"));

        OrderDetailResponse detail = new OrderDetailResponse(order, List.of());

        when(orderService.getOrderWithItems(orderId)).thenReturn(detail);

        mockMvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.id").value(orderId.toString()))
                .andExpect(jsonPath("$.data.status").value("PAID"));
    }
}
