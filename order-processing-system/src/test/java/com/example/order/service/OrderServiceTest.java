package com.example.order.service;

import com.example.order.dto.request.CreateOrderRequest;
import com.example.order.dto.request.OrderItemRequest;
import com.example.order.entity.Order;
import com.example.order.entity.Product;
import com.example.order.producer.OrderEventProducer;
import com.example.order.repository.OrderItemRepository;
import com.example.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductService productService;

    @Mock
    private OrderEventProducer orderEventProducer;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_Success() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId("user1");
        OrderItemRequest itemReq = new OrderItemRequest();
        itemReq.setProductId(1L);
        itemReq.setQuantity(2);
        request.setItems(List.of(itemReq));

        Product mockProduct = new Product();
        mockProduct.setId(1L);
        mockProduct.setPrice(new BigDecimal("100.00"));
        mockProduct.setStock(5);

        when(productService.getProductById(1L)).thenReturn(mockProduct);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order o = i.getArgument(0);
            return o; // Assume ID is already generated in Order constructor
        });

        Order createdOrder = orderService.createOrder(request);

        assertNotNull(createdOrder);
        assertEquals("PENDING", createdOrder.getStatus());
        assertEquals(new BigDecimal("200.00"), createdOrder.getTotalAmount());
        
        verify(orderRepository).save(any(Order.class));
        verify(orderItemRepository).save(any());
        verify(orderEventProducer).publishOrderCreated(any());
    }

    // The insufficient stock test is removed since validation is moved to the consumer.
}
