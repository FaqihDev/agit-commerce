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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import com.example.order.exception.BusinessException;

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

    @Test
    void getOrderById_Success() {
        Order order = new Order();
        order.setId(UUID.randomUUID());
        when(orderRepository.findById(order.getId())).thenReturn(java.util.Optional.of(order));

        Order found = orderService.getOrderById(order.getId());
        assertNotNull(found);
        assertEquals(order.getId(), found.getId());
    }

    @Test
    void getOrderById_NotFound() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(java.util.Optional.empty());

        assertThrows(BusinessException.class, () -> orderService.getOrderById(id));
    }

    @Test
    void updateOrderStatus_Success() {
        Order order = new Order();
        order.setId(UUID.randomUUID());
        when(orderRepository.findById(order.getId())).thenReturn(java.util.Optional.of(order));

        orderService.updateOrderStatus(order.getId(), "PAID");

        assertEquals("PAID", order.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void getOrderWithItems_Success() {
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setStatus("PENDING");
        order.setTotalAmount(new BigDecimal("100.00"));

        com.example.order.entity.OrderItem item = new com.example.order.entity.OrderItem();
        item.setProductId(1L);
        item.setQuantity(2);
        item.setPrice(new BigDecimal("100.00"));

        when(orderRepository.findById(order.getId())).thenReturn(java.util.Optional.of(order));
        when(orderItemRepository.findByOrderId(order.getId())).thenReturn(List.of(item));

        com.example.order.dto.response.OrderDetailResponse detail = orderService.getOrderWithItems(order.getId());

        assertNotNull(detail);
        assertEquals(order.getId(), detail.getId());
        assertEquals("PENDING", detail.getStatus());
        assertEquals(1, detail.getItems().size());
        assertEquals(1L, detail.getItems().get(0).getProductId());
    }
}
