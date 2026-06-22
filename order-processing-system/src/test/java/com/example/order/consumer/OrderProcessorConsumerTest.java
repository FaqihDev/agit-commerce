package com.example.order.consumer;

import com.example.order.dto.message.OrderEventMessage;
import com.example.order.entity.Order;
import com.example.order.entity.OrderItem;
import com.example.order.enums.OrderStatusEnum;
import com.example.order.producer.OrderEventProducer;
import com.example.order.repository.OrderItemRepository;
import com.example.order.service.OrderService;
import com.example.order.service.ProductService;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderProcessorConsumerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private ProductService productService;

    @Mock
    private OrderEventProducer orderEventProducer;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private Channel channel;

    @InjectMocks
    private OrderProcessorConsumer orderProcessorConsumer;

    @Test
    void processOrder_Success() throws IOException {
        OrderEventMessage message = new OrderEventMessage(UUID.randomUUID());
        Order order = new Order();
        order.setId(message.getOrderId());
        order.setStatus(OrderStatusEnum.PENDING.name());

        OrderItem item = new OrderItem();
        item.setProductId(1L);
        item.setQuantity(2);

        when(orderService.getOrderById(message.getOrderId())).thenReturn(order);
        when(orderItemRepository.findByOrderId(order.getId())).thenReturn(List.of(item));

        orderProcessorConsumer.processOrder(message, channel, 123L);

        verify(productService).reduceStock(1L, 2);
        verify(orderService).updateOrderStatus(order.getId(), OrderStatusEnum.PAID.name());
        verify(orderEventProducer).publishOrderPaid(any(OrderEventMessage.class));
        verify(channel).basicAck(123L, false);
    }

    @Test
    void processOrder_IgnoredIfNotPending() throws IOException {
        OrderEventMessage message = new OrderEventMessage(UUID.randomUUID());
        Order order = new Order();
        order.setId(message.getOrderId());
        order.setStatus(OrderStatusEnum.PAID.name());

        when(orderService.getOrderById(message.getOrderId())).thenReturn(order);

        orderProcessorConsumer.processOrder(message, channel, 123L);

        verify(channel).basicAck(123L, false);
        verify(productService, never()).reduceStock(anyLong(), anyInt());
    }

    @Test
    void processOrder_Failed() throws IOException {
        OrderEventMessage message = new OrderEventMessage(UUID.randomUUID());
        Order order = new Order();
        order.setId(message.getOrderId());
        order.setStatus(OrderStatusEnum.PENDING.name());

        when(orderService.getOrderById(message.getOrderId())).thenReturn(order);
        when(orderItemRepository.findByOrderId(order.getId())).thenThrow(new RuntimeException("Database error"));

        orderProcessorConsumer.processOrder(message, channel, 123L);

        verify(orderService).updateOrderStatus(order.getId(), OrderStatusEnum.FAILED.name());
        verify(orderEventProducer).publishOrderFailed(any(OrderEventMessage.class));
        verify(channel).basicReject(123L, false);
    }
}
